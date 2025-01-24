package com.metamate.metamate_service.service.Impl;

import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Project;
import com.metamate.metamate_service.dto.Quiz_Item;
import com.metamate.metamate_service.dto.Receive_Ai_Quiz_Item;
import com.metamate.metamate_service.mapper.QuizMapper;
import com.metamate.metamate_service.service.QuizService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizServiceImpl implements QuizService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    QuizMapper quizMapper;


    @Override
    public int getQuizItemCount(int project_id) {
        logger.debug("QuizServiceImpl :: getQuizItemCount :: project_id = {}", project_id);
        return quizMapper.getQuizItemCount(project_id);
    }

    @Override
    public List<Quiz_Item> getQuizItemList(int project_id, int page_index) {
        logger.debug("QuizServiceImpl :: getQuizItemList :: project_id = {}, page_index={}", project_id,page_index);
        int limit = 10; // 한 페이지당 데이터 10개
        int offset = (page_index - 1) * limit; // offset 계산
        return quizMapper.getQuizItemList(project_id, offset);
    }

    @Override
    public List<Quiz_Item> getAllQuizItemList(int project_id) {
        logger.debug("QuizServiceImpl :: getAllQuizItemList :: project_id = {}", project_id);
        return quizMapper.getAllQuizItemList(project_id);
    }

    @Override
    public boolean insertQuizItem(Quiz_Item quizItem) {
        logger.debug("QuizServiceImpl :: insertQuizItem :: quizItem = {}", quizItem);
        int result = 0;
        try {
            result = quizMapper.insertQuizItem(quizItem);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: insertQuizItem :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean updateQuizItem(Quiz_Item quizItem) {
        logger.debug("QuizServiceImpl :: updateQuizItem :: quizItem = {}", quizItem);
        int result = 0;
        try {
            result = quizMapper.updateQuizItem(quizItem);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: updateQuizItem :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean deleteQuizItem(int item_id) {
        logger.debug("QuizServiceImpl :: deleteQuizItem :: quizItem = {}", item_id);
        int result = 0;
        try {
            result = quizMapper.deleteQuizItem(item_id);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: deleteQuizItem :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public Ai_Quiz_Meta getMyAiQuizMeta(int project_id) {
        logger.debug("QuizServiceImpl :: getMyAiQuizMeta :: project_id = {}", project_id);
        return quizMapper.getMyAiQuizMeta(project_id);
    }

    @Override
    public boolean insertAiQuizMeta(Ai_Quiz_Meta ai_quiz_meta) {
        logger.debug("QuizServiceImpl :: insertAiQuizMeta :: ai_quiz_meta = {}", ai_quiz_meta);
        int result = 0;
        try {
            result = quizMapper.insertAiQuizMeta(ai_quiz_meta);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: insertAiQuizMeta :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean updateAiQuizMeta(Ai_Quiz_Meta ai_quiz_meta) {
        logger.debug("QuizServiceImpl :: updateAiQuizMeta :: ai_quiz_meta = {}", ai_quiz_meta);
        int result = 0;
        try {
            result = quizMapper.updateAiQuizMeta(ai_quiz_meta);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: updateAiQuizMeta :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean insertAiQuizItems(int project_id, List<Receive_Ai_Quiz_Item> quizItems) {
        logger.debug("QuizServiceImpl :: insertAiQuizItems :: quizItems = {}", quizItems);
        int result = 0;
        try {
            result = quizMapper.insertAiQuizItems(project_id,quizItems);
        }catch (Exception e){
            logger.error("QuizServiceImpl :: insertAiQuizItems :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }
}
