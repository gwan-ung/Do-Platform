package com.metamate.metamate_service.service;

import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Project;
import com.metamate.metamate_service.dto.Quiz_Item;
import com.metamate.metamate_service.dto.Receive_Ai_Quiz_Item;

import java.util.List;

public interface QuizService {

    int getQuizItemCount(int project_id);
    List<Quiz_Item> getQuizItemList(int project_id, int page_index);
    List<Quiz_Item> getAllQuizItemList(int project_id);
    boolean insertQuizItem(Quiz_Item quizItem);
    boolean updateQuizItem(Quiz_Item quizItem);
    boolean deleteQuizItem(int item_id);

    Ai_Quiz_Meta getMyAiQuizMeta(int project_id);
    boolean insertAiQuizMeta(Ai_Quiz_Meta ai_quiz_meta);
    boolean updateAiQuizMeta(Ai_Quiz_Meta ai_quiz_meta);

    boolean insertAiQuizItems(int project_id, List<Receive_Ai_Quiz_Item> quizItems);
}
