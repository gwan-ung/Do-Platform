package com.metamate.metamate_service.service;

import com.metamate.metamate_service.dto.*;

import java.util.List;

public interface MainService {
    List<Project> getMyProjectList(String user_id);
    List<Project> getMyTrashProjectList(String user_id);
    List<Project> getMyFavoriteProjectList(String user_id);


    List<Deleted_Project> getMyDeletedProjectList(String user_id);

    List<Folder> getMyFolderList(String user_id);
    List<Project> getMyFolderProjectList(int folder_id);
    List<Folder_Project> getAllFolderProjectList(String user_id);

    void createMyFolder(Folder folder);
    boolean deleteMyFolder(String user_id, int folder_id);
    boolean changeFolderName(int folder_id, String folder_name);

    boolean createMyFolderProject(int folder_id, int project_id);
    boolean deleteMyFolderProject(int folder_id, int project_id);

    boolean changeMaxParticipants(int project_id, int max_participants);
    boolean changeProjectFavorite(int project_id, int is_favorite);
    boolean changeProjectDeleted(int project_id, int is_deleted);
    boolean projectPermanentDelete(int project_id);
    boolean changeProjectName(int project_id, String project_name);

    List<Theme> getThemeList();
    List<Content> getContentList();

    boolean recoveryProject(int project_id);
    boolean tempDeletedProject(int project_id);
    boolean removeProject(int project_id);

    Project getProject(int project_id);

    void createProject(Project project);
}
