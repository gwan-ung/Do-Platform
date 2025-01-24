package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class Project_Object {
    private String user_id;
    private String name;
    private int theme;
    private int content;
    private int max_participants;
}
