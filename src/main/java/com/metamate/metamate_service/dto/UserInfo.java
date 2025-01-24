package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class UserInfo {
    private String user_id;
    private String user_name;
    private Boolean is_sns;
}
