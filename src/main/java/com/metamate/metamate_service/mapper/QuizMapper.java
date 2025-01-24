package com.metamate.metamate_service.mapper;

import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Project;
import com.metamate.metamate_service.dto.Quiz_Item;
import com.metamate.metamate_service.dto.Receive_Ai_Quiz_Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuizMapper {
    int getQuizItemCount(int project_id);
    List<Quiz_Item> getQuizItemList(@Param("project_id")int project_id, @Param("offset")int offset);
    List<Quiz_Item> getAllQuizItemList(@Param("project_id")int project_id);
    int insertQuizItem(@Param("quizItem") Quiz_Item quizItem);
    int updateQuizItem(@Param("quizItem") Quiz_Item quizItem);
    int deleteQuizItem(@Param("quiz_id") int quiz_id);

    Ai_Quiz_Meta getMyAiQuizMeta(@Param("project_id")int project_id);
    int insertAiQuizMeta(@Param("ai_quiz_meta")Ai_Quiz_Meta  ai_quiz_meta);
    int updateAiQuizMeta(@Param("ai_quiz_meta")Ai_Quiz_Meta  ai_quiz_meta);

    int insertAiQuizItems(@Param("project_id") int project_id, @Param("quizItems")List<Receive_Ai_Quiz_Item> quizItems);
}
