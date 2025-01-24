package com.metamate.metamate_service.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Ai_Quiz_Question {
    private String question;            // 문제 텍스트
    private List<String> options;           // 선택지 (배열 형태)
    private String answer;              // 정답
    private Map<String, String> explanations;  // 각 선택지에 대한 해설
}
