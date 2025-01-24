package com.metamate.metamate_service.mapper;

import com.metamate.metamate_service.dto.Deleted_Project;
import com.metamate.metamate_service.dto.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(String username);

    //User findByUserId(String user_id);

    void insertUser(User user);
    int registerEmailUser(User user);
    User findByVerificationCode(String code);
    void verifyUser(String user_id);
    void updateUserVerify(@Param("user_id") String user_id,@Param("code") String code);
    int updateUserName(@Param("user_id") String user_id,@Param("name") String name);
    int updatePassword(@Param("user_id") String user_id,@Param("password") String password);
    int deleteUser(String user_id);

    //int retryVerifyCode(@Param("email") String email,@Param("verify_code") String verifyCode);
    //Deleted_Project getDeletedProject(int project_id);
}
