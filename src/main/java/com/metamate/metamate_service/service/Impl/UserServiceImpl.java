package com.metamate.metamate_service.service.Impl;

import com.metamate.metamate_service.dto.Deleted_Project;
import com.metamate.metamate_service.dto.User;
import com.metamate.metamate_service.mapper.UserMapper;
import com.metamate.metamate_service.service.EmailService;
import com.metamate.metamate_service.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public User findByUserId(String username) {
        logger.debug("UserServiceImpl :: findByUserId :: START");
        return userMapper.findByUsername(username);
    }

    @Override
    public User registerUser(User user) {
//        if (userMapper.findByUsername(user.getUser_id()) != null) {
//            throw new IllegalArgumentException("User ID already exists");
//        }
//
//        userMapper.insertUser(user);

       logger.debug("UserServiceImpl :: registerUser :: start");

        if (userMapper.findByUsername(user.getUser_id()) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        //String verificationCode = UUID.randomUUID().toString();
        //logger.debug("UserServiceImpl :: registerUser :: verificationCode = {}",verificationCode);
        // 이메일 전송

        logger.debug("UserServiceImpl :: registerUser :: email = {}",user.getUser_id());
        String verificationCode = "";
        try {
            verificationCode = emailService.sendVerificationEmail(user.getUser_id());
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("registerUser :: {}",e.getMessage());
        }

        if(verificationCode != null){
            User newUser = new User();
            newUser.setUser_id(user.getUser_id());
            if(user.getLogin_type() != "SNS"){
                newUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
            newUser.setName(user.getName());
            newUser.setLogin_type(user.getLogin_type());
            newUser.setVerified(0);
            newUser.setVerification_code(verificationCode);
            logger.debug("UserServiceImpl :: registerUser :: newUser = {}",newUser);
            userMapper.insertUser(newUser);
       }else{
           logger.debug("UserServiceImpl :: registerUser :: verificationCode is null");
       }

        logger.debug("UserServiceImpl :: registerUser :: end");
        return user;
    }

    @Override
    public boolean registerEmailUser(User user) {
        logger.debug("UserServiceImpl :: registerEmailUser ::user = {}", user);
        int result = userMapper.registerEmailUser(user);
        return result > 0;
    }

    @Override
    public boolean verifyUser(String code) {
        logger.debug("UserServiceImpl :: verifyUser ::code = {}", code);
        User user = userMapper.findByVerificationCode(code);
        if (user == null) {
            return false;
        }

        userMapper.verifyUser(user.getUser_id());
        return true;
    }

    @Override
    public void updateUserVerify(String user_id,String code) {
        logger.debug("UserServiceImpl :: updateUserVerify :: user = {}, code = {}",user_id,code);
        userMapper.updateUserVerify(user_id,code);
    }

    @Override
    public boolean updateUserName(String user_id, String name) {
        logger.debug("UserServiceImpl :: updateUserName :: user = {}, name = {}",user_id,name);
        int result = 0;
        try {
            result = userMapper.updateUserName(user_id,name);

        }catch (Exception e){
            logger.error("UserServiceImpl :: updateUserName :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean updatePassword(String user_id, String password) {
        logger.debug("UserServiceImpl :: updatePassword :: user = {}, password = {}",user_id,password);
        int result = 0;
        try {
            result = userMapper.updatePassword(user_id,password);

        }catch (Exception e){
            logger.error("UserServiceImpl :: updatePassword :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean deleteUser(String user_id) {
        logger.debug("UserServiceImpl :: deleteUser :: user = {}",user_id);
        int result = 0;
        try {
             result = userMapper.deleteUser(user_id);
        }catch (Exception e){
            logger.error("UserServiceImpl :: deleteUser :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean retryVerifyCode(String email) {
        logger.debug("UserServiceImpl :: retryVerifyCode :: email = {}",email);
        int result = 0;
        try {
            String verifyCode = emailService.sendVerificationEmail(email);

            userMapper.updateUserVerify(email,verifyCode);


        }catch (Exception e){
            logger.error("UserServiceImpl :: retryVerifyCode :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean resetPassword(String user_id) {
        logger.debug("UserServiceImpl :: resetPassword :: user_id = {}",user_id);
        int result = 0;
        try {
            // 임시 비밀번호 생성
            String tempPassword = emailService.sendInitPasswordEmail(user_id);

            result = userMapper.updatePassword(user_id,bCryptPasswordEncoder.encode(tempPassword));


        }catch (Exception e){
            logger.error("UserServiceImpl :: resetPassword :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[6];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
