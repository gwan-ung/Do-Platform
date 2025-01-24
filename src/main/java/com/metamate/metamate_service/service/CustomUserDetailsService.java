package com.metamate.metamate_service.service;

import com.metamate.metamate_service.dto.User;
import com.metamate.metamate_service.handler.EmailNotVerifiedException;
import com.metamate.metamate_service.mapper.UserMapper;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("CustomUserDetailsService :: loadUserByUsername :: START");
        User user = userMapper.findByUsername(username);
        if (user == null) {
            logger.debug("CustomUserDetailsService :: loadUserByUsername :: user null");
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        if(user.getVerified() == 0){
            logger.debug("CustomUserDetailsService :: loadUserByUsername :: verified false");
            String verificationCode = "";
            try {
                verificationCode = emailService.sendVerificationEmail(user.getUser_id());
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            if(verificationCode != null){
                userMapper.updateUserVerify(user.getUser_id(),verificationCode);
                throw new EmailNotVerifiedException("Please check the verification code in your email!");
            }else{
                throw new EmailNotVerifiedException("Failed to issue verification code. Please log in again.");
            }

        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

//        return new org.springframework.security.core.userdetails.User(
//                user.getUser_id(), // Spring Security UserDetails의 username으로 사용
//                user.getPassword(),
//                authorities
//        );
        return buildUserForAuthentication(user, getUserAuthority(authorities));
    }

    private List<GrantedAuthority> getUserAuthority(Collection<? extends GrantedAuthority> userRole){
        logger.debug("CustomUserDetailsService :: getUserAuthority :: userRole = {}",userRole);
        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
        for(GrantedAuthority authority: userRole){
            logger.debug("CustomUserDetailsService :: getUserAuthority :: authority = {}",authority.getAuthority());
            roles.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(roles);
        logger.debug("CustomUserDetailsService :: getUserAuthority :: grantedAuthorities = {}",grantedAuthorities);
        return grantedAuthorities;
    }

    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities){
        logger.debug("CustomUserDetailsService :: buildUserForAuthentication :: user.getUser_id() ={} , " +
                "user.getPassword() = {}, authorities = {}",user.getUser_id(),user.getPassword(),authorities);
        return new org.springframework.security.core.userdetails.User(user.getUser_id(), user.getPassword(), authorities);
    }
}
