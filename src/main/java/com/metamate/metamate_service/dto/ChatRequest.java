package com.metamate.metamate_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRequest {
    private Long id;
    private String request;
    private String response;
    private LocalDateTime createdAt;
}
