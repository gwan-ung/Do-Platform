package com.metamate.metamate_service.service.Impl;

import com.metamate.metamate_service.dto.*;
import com.metamate.metamate_service.mapper.MainMapper;
import com.metamate.metamate_service.service.MainService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainServiceImpl implements MainService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private MainMapper mainMapper;

    @Override
    public List<Project> getMyProjectList(String user_id) {
        logger.debug("MainServiceImpl :: getMyProjectList :: user_id = {}",user_id);
        return mainMapper.getMyProjectList(user_id);
    }

    @Override
    public List<Project> getMyTrashProjectList(String user_id) {
        logger.debug("MainServiceImpl :: getMyTrashProjectList :: user_id = {}",user_id);
        return mainMapper.getMyTrashProjectList(user_id);
    }

    @Override
    public List<Deleted_Project> getMyDeletedProjectList(String user_id) {
        logger.debug("MainServiceImpl :: getMyDeletedProjectList :: user_id = {}",user_id);
        return mainMapper.getMyDeletedProjectList(user_id);
    }

    @Override
    public List<Project> getMyFavoriteProjectList(String user_id) {
        logger.debug("MainServiceImpl :: getMyFavoriteProjectList :: user_id = {}",user_id);
        return mainMapper.getMyFavoriteProjectList(user_id);
    }

    @Override
    public List<Folder> getMyFolderList(String user_id) {
        logger.debug("MainServiceImpl :: getMyFolderList :: user_id = {}",user_id);
        return mainMapper.getMyFolderList(user_id);
    }

    @Override
    public List<Project> getMyFolderProjectList(int folder_id) {
        logger.debug("MainServiceImpl :: getMyFolderProjectList :: folder_id = {}",folder_id);
        return mainMapper.getMyFolderProjectList(folder_id);
    }

    @Override
    public List<Folder_Project> getAllFolderProjectList(String user_id) {
        logger.debug("MainServiceImpl :: getAllFolderProjectList :: user_id = {}",user_id);
        return mainMapper.getAllFolderProjectList(user_id);
    }

    @Override
    public void createMyFolder(Folder folder) {
        logger.debug("MainServiceImpl :: createMyFolder :: {}",folder);
        mainMapper.createMyFolder(folder);
    }

    @Override
    public boolean deleteMyFolder(String user_id, int folder_id) {
        logger.debug("MainServiceImpl :: deleteMyFolder :: user_id = {}, folder_id = {}",user_id,folder_id);
        int result = mainMapper.deleteMyFolder(user_id,folder_id);
        return result > 0;
    }

    @Override
    public boolean changeFolderName(int folder_id, String folder_name) {
        logger.debug("MainServiceImpl :: changeFolderName :: folder_id = {}, folder_name = {}",folder_id,folder_name);
        int result = mainMapper.changeFolderName(folder_id,folder_name);
        return result > 0;
    }

    @Override
    public boolean createMyFolderProject(int folder_id, int project_id) {
        logger.debug("MainServiceImpl :: createMyFolderProject :: folder_id = {}, project_id = {}",folder_id,project_id);
        int result = mainMapper.createMyFolderProject(folder_id,project_id);
        return result > 0;
    }

    @Override
    public boolean deleteMyFolderProject(int folder_id, int project_id) {
        logger.debug("MainServiceImpl :: deleteMyFolderProject :: folder_id = {}, project_id = {}",folder_id,project_id);
        int result = mainMapper.deleteMyFolderProject(folder_id,project_id);
        return result > 0;
    }


    @Override
    public boolean changeMaxParticipants(int project_id, int max_participants) {
        logger.debug("MainServiceImpl :: changeMaxParticipants :: project_id = {}, max_participants = {}",
                project_id,
                max_participants);
        int result = 0;
        try {
            result = mainMapper.changeMaxParticipants(project_id,max_participants);
        }catch (Exception e){
            logger.error("MainServiceImpl :: changeMaxParticipants :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean changeProjectFavorite(int project_id, int is_favorite) {
        logger.debug("MainServiceImpl :: changeProjectFavorite :: project_id = {}, is_favorite = {}",
                project_id,
                is_favorite);
        int result = 0;
        try {
            result = mainMapper.changeProjectFavorite(project_id,is_favorite);
        }catch (Exception e){
            logger.error("MainServiceImpl :: changeProjectFavorite :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean changeProjectDeleted(int project_id, int is_deleted) {
        logger.debug("MainServiceImpl :: changeProjectDeleted :: project_id = {}, is_deleted = {}",
                project_id,
                is_deleted);
        int result = 0;
        try {
            result = mainMapper.changeDeletedStatus(project_id,is_deleted);
        }catch (Exception e){
            logger.error("MainServiceImpl :: changeProjectDeleted :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean projectPermanentDelete(int project_id) {
        logger.debug("MainServiceImpl :: projectPermanentDelete :: project_id = {}", project_id);
        int result = 0;
        try {
            result = mainMapper.projectPermanentDelete(project_id);
        }catch (Exception e){
            logger.error("MainServiceImpl :: projectPermanentDelete :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean changeProjectName(int project_id, String project_name) {
        logger.debug("MainServiceImpl :: changeProjectName :: project_id = {}, project_name = {}",project_id,project_name);
        int result = mainMapper.changeProjectName(project_id,project_name);
        return result > 0;
    }

    @Override
    public List<Theme> getThemeList() {
        logger.debug("MainServiceImpl :: getThemeList");
        return mainMapper.getThemeList();
    }

    @Override
    public List<Content> getContentList() {
        logger.debug("MainServiceImpl :: getContentList");
        return mainMapper.getContentList();
    }

    @Override
    public boolean recoveryProject(int project_id) {
        //휴지통에서 프로젝트 복원
        logger.debug("MainServiceImpl :: recoveryProject :: project_id = {}", project_id);
        int result = 0;
        try {
            result = mainMapper.changeDeletedStatus(project_id,0);
        }catch (Exception e){
            logger.error("MainServiceImpl :: recoveryProject :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean tempDeletedProject(int project_id) {
        //프로젝트 휴지통
        logger.debug("MainServiceImpl :: tempDeletedProject :: project_id = {}", project_id);
        int result = 0;
        try {
            result = mainMapper.changeDeletedStatus(project_id,1);
        }catch (Exception e){
            logger.error("MainServiceImpl :: tempDeletedProject :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public boolean removeProject(int project_id) {
        //프로젝트 완전삭제
        logger.debug("MainServiceImpl :: removeProject :: project_id = {}", project_id);
        int result = 0;
        try {
            result = mainMapper.removeProject(project_id);
        }catch (Exception e){
            logger.error("MainServiceImpl :: removeProject :: Exception = {}",e.getMessage());
            return false;
        }
        return result > 0;
    }

    @Override
    public Project getProject(int project_id) {
        logger.debug("MainServiceImpl :: getProject :: project_id = {}", project_id);
        return mainMapper.getProject(project_id);
    }

    @Override
    public void createProject(Project project) {
        logger.debug("MainServiceImpl :: call the createProject");
        mainMapper.createProject(project);

    }
}
