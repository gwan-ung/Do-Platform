package com.metamate.metamate_service.service;


import com.metamate.metamate_service.dto.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HttpServletResponse response;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("CustomOAuth2UserService :: OAuth2UserRequest = {}",userRequest);
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        //logger.debug("CustomOAuth2UserService :: oAuth2User = {}",oAuth2User);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.debug("CustomOAuth2UserService :: registrationId = {}",registrationId);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.debug("CustomOAuth2UserService :: attributes = {}",attributes);

        attributes = changeAttributes(registrationId, attributes);
        // SNS 제공자별 사용자 ID와 이름 추출
        String userId = getUserIdFromAttributes(registrationId, attributes, "email");
        String name = getUserIdFromAttributes(registrationId, attributes, "name");//(String) attributes.get("name");
        logger.debug("userId : {} , name : {}",userId,name);
        if (userId == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("10002","해당 사이트의 계정이 있는지 확인하세요.",null)
            );
        }
        // 사용자 정보 생성 및 저장/업데이트
        User user = userService.findByUserId(userId);

        if(user == null){
            logger.debug("CustomOAuth2UserService :: loadUser :: user == null");
            user = new User();
            user.setUser_id(userId);
            user.setName(name != null ? name : "SNS User");
            user.setRole("ROLE_USER"); // 기본 역할 설정
            user.setLogin_type("SNS");
            user.setVerified(0);
            userService.registerUser(user);
                throw new OAuth2AuthenticationException(
            new OAuth2Error("10001",userId,null)
            );
        }else{//이미 계정이 있을때
            logger.debug("CustomOAuth2UserService :: loadUser :: user not null");
            if(user.getVerified() == 0){//이메일 인증이 안된 상태
                logger.debug("CustomOAuth2UserService :: loadUser :: user.getVerified() == 0");
                throw new OAuth2AuthenticationException(new OAuth2Error("10001",userId,null));
            }else{//이메일 인증이 된 상태
                logger.debug("CustomOAuth2UserService :: loadUser :: user.getVerified() == 1");

            }

        }

        return new DefaultOAuth2User(
                Arrays.asList(
                        new OAuth2UserAuthority(attributes), // 기본 속성
                        new SimpleGrantedAuthority("ROLE_USER") // 권한 추가
                ),
                attributes,
                getKey(registrationId) // 사용자 고유 식별자로 사용될 키
        );
    }
    private Map<String, Object> changeAttributes(String registrationId,Map<String, Object> attributes) {
        logger.debug("changeAttributes :: registrationId = {}",registrationId);
        if ("google".equals(registrationId)) {
            return attributes;
        } else if ("naver".equals(registrationId)) {
            return (Map<String, Object>) attributes.get("response");
        } else if ("kakao".equals(registrationId)) {
            return processKakaoAttributes(attributes);
        }
        return null;
    }

    private Map<String, Object> processKakaoAttributes(Map<String, Object> attributes) {
        // 수정 가능한 맵으로 복사하여 사용
        Map<String, Object> mutableAttributes = new HashMap<>(attributes);

        // kakao_account 내부의 속성을 수정 가능한 맵으로 복사하여 처리
        Map<String, Object> kakaoAccount = (Map<String, Object>) mutableAttributes.get("kakao_account");
        if (kakaoAccount != null) {
            logger.debug("processKakaoAttributes :: kakaoAccount = {}", kakaoAccount);

            // 이메일 값이 존재하면 추가
            String email = (String) kakaoAccount.get("email");
            if (email != null) {
                mutableAttributes.put("email", email); // 수정 가능한 맵에 이메일 추가
                logger.debug("Email extracted: {}", email);
            }

            // 프로필 닉네임 추출
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                String nickname = (String) profile.get("nickname");
                if (nickname != null) {
                    mutableAttributes.put("name", nickname); // 수정 가능한 맵에 닉네임 추가
                    logger.debug("Nickname extracted: {}", nickname);
                }
            }
        } else {
            logger.debug("kakao_account is null or not provided in attributes");
        }

        return mutableAttributes;
    }

    private String getUserIdFromAttributes(String registrationId, Map<String, Object> attributes,String field) {
        logger.debug("getUserIdFromAttributes :: registrationId = {}",registrationId);
        if ("google".equals(registrationId)) {
            logger.debug("getUserIdFromAttributes :: google");
            return (String) attributes.get(field);
        } else if ("naver".equals(registrationId)) {
            logger.debug("getUserIdFromAttributes :: naver");
            //Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) attributes.get(field);
        } else if ("kakao".equals(registrationId)) {
            logger.debug("getUserIdFromAttributes :: kakao");
            //Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account.profile");
            return attributes.get(field).toString();
        }
        return null;
    }

    private String getKey(String registrationId) {
        logger.debug("getKey :: registrationId = {}",registrationId);
        if ("google".equals(registrationId)) {
            return "sub";
        } else if ("naver".equals(registrationId)) {
            return "id";
        } else if ("kakao".equals(registrationId)) {
            return "id";
        }
        return null;
    }
}
