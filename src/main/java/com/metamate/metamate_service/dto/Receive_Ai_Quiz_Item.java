package com.metamate.metamate_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Receive_Ai_Quiz_Item {
    private String question;
    private List<String> options;
    private String answer;
    private String explanations;
}
