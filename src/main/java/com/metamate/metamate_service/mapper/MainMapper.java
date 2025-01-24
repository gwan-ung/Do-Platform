package com.metamate.metamate_service.mapper;

import com.metamate.metamate_service.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MainMapper {
    List<Project> getMyProjectList(@Param("user_id") String user_id);
    List<Project> getMyTrashProjectList(@Param("user_id") String user_id);

    List<Deleted_Project> getMyDeletedProjectList(@Param("user_id") String user_id);
    List<Project> getMyFavoriteProjectList(@Param("user_id") String user_id);
    List<Folder> getMyFolderList(@Param("user_id") String user_id);
    List<Project> getMyFolderProjectList(@Param("folder_id") int folder_id);
    List<Folder_Project> getAllFolderProjectList(@Param("user_id") String user_id);

    void createMyFolder(@Param("folder") Folder folder);
    int deleteMyFolder(@Param("user_id") String user_id,@Param("folder_id") int folder_id);
    int changeFolderName(@Param("folder_id") int folder_id,@Param("folder_name") String folder_name);
    int changeProjectName(@Param("project_id") int project_id,@Param("project_name") String project_name);
    int createMyFolderProject(@Param("folder_id") int folder_id,@Param("project_id") int project_id);
    int deleteMyFolderProject(@Param("folder_id") int folder_id,@Param("project_id") int project_id);

    int changeMaxParticipants(@Param("project_id") int project_id, @Param("max_participants") int max_participants);
    int changeProjectFavorite(@Param("project_id") int project_id, @Param("is_favorite") int is_favorite);
    int changeDeletedStatus(@Param("project_id") int project_id, @Param("is_deleted") int is_deleted);
    int projectPermanentDelete(@Param("project_id") int project_id);

    List<Theme> getThemeList();
    List<Content> getContentList();

    int removeProject(@Param("project_id") int project_id);

    Project getProject(@Param("project_id") int project_id);

    void createProject(@Param("project")Project project);
}
