package com.metamate.metamate_service.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomOAuthSnsFailureHandler extends SimpleUrlAuthenticationFailureHandler  {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException  exception) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            OAuth2Error error = oauth2Exception.getError();
            String errorCode = error.getErrorCode();
            String errorDescription = URLEncoder.encode(error.getDescription(), StandardCharsets.UTF_8);

            logger.debug("CustomOAuthSnsFailureHandler :: START");

            // 리디렉션 URL 설정
            String redirectUrl = "/error-login?message=" + errorDescription;
            if(errorCode == "10001"){
                redirectUrl = "/sign-up-email-confirm?email="+errorDescription;
            }
            if(errorCode == "10002"){
                redirectUrl = "/error-login?message="+errorDescription;
            }
            logger.debug("CustomOAuthSnsFailureHandler :: redirectUrl = {}",redirectUrl);
            // 리디렉션 수행
            response.sendRedirect(redirectUrl);

            logger.debug("CustomOAuthSnsFailureHandler :: response.sendRedirect");
        } else {
            logger.debug("CustomOAuthSnsFailureHandler :: onAuthenticationFailure");
            // OAuth2 예외가 아닌 경우 기본 실패 핸들러 동작 수행
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
