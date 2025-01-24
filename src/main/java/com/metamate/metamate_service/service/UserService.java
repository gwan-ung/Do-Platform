package com.metamate.metamate_service.service;

import com.metamate.metamate_service.dto.User;
import org.springframework.stereotype.Service;


public interface UserService {
    User findByUserId(String username);
    User registerUser(User user);
    boolean registerEmailUser(User user);
    boolean verifyUser(String code);
    void updateUserVerify(String user_id, String code);
    boolean updateUserName(String user_id, String name);
    boolean updatePassword(String user_id, String password);
    boolean deleteUser(String user_id);

    boolean retryVerifyCode(String email);

    boolean resetPassword(String user_id);
}
