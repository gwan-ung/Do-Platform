package com.metamate.metamate_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Folder {
    private int id;
    private String user_id;
    private String name;
    private List<String> folder_project;
}
