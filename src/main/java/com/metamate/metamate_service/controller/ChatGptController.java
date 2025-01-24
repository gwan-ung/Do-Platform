package com.metamate.metamate_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Ai_Quiz_Question;
import com.metamate.metamate_service.service.ChatGptService;
import com.metamate.metamate_service.service.QuizService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ChatGptController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private ChatGptService chatGptService;

    @Autowired
    private QuizService quizService;

    @GetMapping("/quiz/create-ai-quiz")
    public String chat(@RequestParam("project_id") int project_id, Model model) throws JsonProcessingException {
        logger.debug("ChatGptController :: chat-gpt :: project_id = {}",project_id);
        Ai_Quiz_Meta aiQuizMeta = quizService.getMyAiQuizMeta(project_id);
        if(aiQuizMeta != null){
            List<Ai_Quiz_Question> aiQuizQuestionList = chatGptService.getChatGptResponse(aiQuizMeta);

            logger.debug("aiQuizQuestionList = {}",aiQuizQuestionList);

            model.addAttribute("aiQuizQuestionList",aiQuizQuestionList);
            model.addAttribute("project_id",project_id);
            model.addAttribute("ai_meta_id", aiQuizMeta.getId());
        }else{
            model.addAttribute("aiQuizQuestionList",null);
            model.addAttribute("project_id",project_id);
        }
        return "/quiz/create_ai_quiz2";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat"; // chat.html 파일 반환
    }
}
