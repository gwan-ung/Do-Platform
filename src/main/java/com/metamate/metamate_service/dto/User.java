package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class User {
    private String user_id;
    private String password;
    private String name;
    private String role;
    private String login_type;//regist_self, sns
    private int verified;//0: false, 1:true
    private String verification_code;
    private String join_date;
}
