package com.metamate.metamate_service.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        logger.debug("CustomAuthenticationFailureHandler :: onAuthenticationFailure :: START");
        String error = "authentication_error";
        String message = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        if (exception instanceof EmailNotVerifiedException) {
            error = "email_not_verified";
            message = "Your email is not verified. Please check your email!";
        }
        //response.sendRedirect("/message?error=Authentication Error&error_message=" + exception.getMessage()); // 로그인 실패 시 리디렉션
        // 리디렉션 URL 설정
//        String redirectUrl = "/login?message=" + error + "&error_message=" + message;
        message = URLEncoder.encode("로그인 정보를 정확히 입력하세요.", StandardCharsets.UTF_8);
        String redirectUrl = "/error-login?message="+message;
        // 리디렉션 수행
        response.sendRedirect(redirectUrl);
    }
}
