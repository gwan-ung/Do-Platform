package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class Quiz_Item {
    private int id;
    private int project_id;
    private int is_ai_created;
    private String question;
    private String answer;
    private String explanation;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String image_url;
    private String created_at;
}
