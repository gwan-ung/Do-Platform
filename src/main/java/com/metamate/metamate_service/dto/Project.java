package com.metamate.metamate_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Project {
    private int id;
    private String user_id;
    private String name;
    private String description;
    private String server_url;
    private int is_quiz_included;
    private int is_favorite;
    private int max_participants;
    private int theme;
    private int content;
    private int is_deleted;
    private String created_at;
    private String updated_at;

}
