package com.metamate.metamate_service.config;

import com.metamate.metamate_service.handler.CustomAuthenticationFailureHandler;
import com.metamate.metamate_service.handler.CustomAuthenticationSuccessHandler;
import com.metamate.metamate_service.handler.CustomOAuthSnsFailureHandler;
import com.metamate.metamate_service.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomOAuthSnsFailureHandler snsFailureHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler,
                          CustomOAuthSnsFailureHandler snsFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.snsFailureHandler = snsFailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/v1/**",
                                "/index",
                                "/login",
                                "/login/**",
                                "/sign-up",
                                "/sign-up-email",
                                "/register-user-email",
                                "/reset-password",
                                "../assets/**",
                                "/assets/**",
                                "static/assets/**",
                                "static/**",
                                "/favicon.ico",
                                "/register",
                                "/sign-up-email-confirm",
                                "/oauth2/**",
                                "/verify/**",
                                "/message",
                                "/email-login",
                                "/error-login/**"
                                ).permitAll() // 공개 경로 설정
                        .requestMatchers("/main/**","/user/**","/api/**","/project/**","/update-password").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/init-login") // 커스텀 로그인 페이지
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler) // 로그인 성공 핸들러
                        .failureHandler(failureHandler) // 로그인 실패 핸들러
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                ).oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 소셜 로그인도 동일한 로그인 페이지 사용
                        .defaultSuccessUrl("/main/project") // 소셜 로그인 성공 후 리디렉션 경유
                        .failureHandler(snsFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/login?logout") // 로그아웃 성공 후 리디렉션
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID") // 쿠키 삭제
                        .permitAll()
                );

        return http.build();
    }
}
