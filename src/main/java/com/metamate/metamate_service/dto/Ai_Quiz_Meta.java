package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class Ai_Quiz_Meta {
    private int id;
    private int project_id;
    private int num_questions;
    private int num_options;
    private String difficulty_level;
    private String grade_level;
    private String topic;
    private String objective;
    private String content;
    private String keyword;
    private String generated_at;

}
