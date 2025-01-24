package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class UserView {
    private String user_id;
    private String status;
    private String join_date;
    private String delete_date;
}
