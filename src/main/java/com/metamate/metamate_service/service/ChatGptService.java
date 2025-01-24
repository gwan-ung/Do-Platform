package com.metamate.metamate_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Ai_Quiz_Question;

import java.util.List;

public interface ChatGptService {
    List<Ai_Quiz_Question> getChatGptResponse(Ai_Quiz_Meta aiQuizMeta) throws JsonProcessingException;
}
