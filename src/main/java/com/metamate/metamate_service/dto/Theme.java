package com.metamate.metamate_service.dto;

import lombok.Data;

@Data
public class Theme {
    private int id;
    private int subject;
    private String name;
    private String description;
    private String image_url;
}
