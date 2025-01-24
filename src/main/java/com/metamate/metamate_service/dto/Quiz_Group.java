package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class Quiz_Group {
    private int id;
    private int project_id;
    private String user_id;
    private String name;
    private int is_ai_generated;
    private String updated_at;
    private String created_at;
}
