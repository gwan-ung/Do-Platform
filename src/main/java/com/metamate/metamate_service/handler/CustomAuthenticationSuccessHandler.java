package com.metamate.metamate_service.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.CookieRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private RequestCache requestCache = new CookieRequestCache();
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final String DEFAULT_LOGIN_SUCCESS_URL ="/main/project";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        //response.sendRedirect("/home"); // 로그인 성공 후 리디렉션

        clearAuthenticationAttributes(request);
        redirectStrategy(request,response,authentication);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null){
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

    private void redirectStrategy(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        SavedRequest savedRequest = requestCache.getRequest(request,response);
        if(savedRequest == null){
            logger.debug("CustomAuthenticationSuccessHandler :: redirectStrategy :: savedRequest null");
            redirectStrategy.sendRedirect(request,response,DEFAULT_LOGIN_SUCCESS_URL);
        }else{
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            logger.debug("CustomAuthenticationSuccessHandler :: redirectStrategy :: not null ::roles = {}",roles);
            if(roles.contains("ROLE_ADMIN")){
                logger.debug("CustomAuthenticationSuccessHandler :: redirectStrategy :: roll ROLE_ADMIN");
                redirectStrategy.sendRedirect(request,response,"/main/project");
            }else if(roles.contains("ROLE_USER")){
//                redirectStrategy.sendRedirect(request,response,"/home/user");
                logger.debug("CustomAuthenticationSuccessHandler :: redirectStrategy :: roll ROLE_USER");
                redirectStrategy.sendRedirect(request,response,"/main/project");
            }else{
                //redirectStrategy.sendRedirect(request,response,"/home/guest");
                logger.debug("CustomAuthenticationSuccessHandler :: redirectStrategy :: roll X");
                redirectStrategy.sendRedirect(request,response,"/init-login");
            }
        }
    }
}
