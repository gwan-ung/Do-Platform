package com.metamate.metamate_service.controller;

import com.metamate.metamate_service.dto.*;
import com.metamate.metamate_service.service.FirebaseStorageService;
import com.metamate.metamate_service.service.MainService;
import com.metamate.metamate_service.service.QuizService;
import com.metamate.metamate_service.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/main")
public class MainController {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    UserService userService;

    @Autowired
    MainService mainService;

    @Autowired
    QuizService quizService;

    @Autowired
    FirebaseStorageService firebaseStorageService;

    @GetMapping("/project")
    public String project(Model model){
        logger.debug("MainController :: project =/main/project");

        // 현재 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("auth = {}", auth);

        String userName = "";
        String userId = "";
        Boolean isSns=false;
        // 인증 객체의 타입에 따라 분기 처리
        if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            logger.debug("org.springframework.security.core.userdetails.User");
            // 일반 로그인 (UserDetails 타입)
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) auth.getPrincipal();

            // userName과 userId 가져오기
            userId = userDetails.getUsername();
            // 일반 로그인에서는 userId를 기준으로 DB에서 추가 정보 가져오기
            User user = userService.findByUserId(userId);
            userName = user.getName();

        } else if (auth.getPrincipal() instanceof OAuth2User) {
            logger.debug("OAuth2User");
            // OAuth2 로그인 (OAuth2User 타입)
            OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();

            // OAuth2User의 Attribute에서 이름 추출
            Map<String, Object> attributes = oAuth2User.getAttributes();

            if (attributes.containsKey("name")) {
                // 일반적으로 OAuth2 프로바이더에서 "name" 속성을 제공
                userName = (String) attributes.get("name");
            } else if (attributes.containsKey("login")) {
                // GitHub의 경우 "login" 속성
                userName = (String) attributes.get("login");
            }

            // userId는 provider-specific (e.g., email, id 등)
            if (attributes.containsKey("email")) {
                userId = (String) attributes.get("email");
            } else if (attributes.containsKey("id")) {
                userId = attributes.get("id").toString();
            }
            isSns = true;
            User user = userService.findByUserId(userId);
            userName = user.getName();
        }

        // UserInfo 객체 생성
        UserInfo userInfo = new UserInfo();
        userInfo.setUser_id(userId);
        userInfo.setUser_name(userName);
        userInfo.setIs_sns(isSns);

        List<Folder> folderList = mainService.getMyFolderList(userId);
        // Model에 추가
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("folderList", folderList);

        return "main/project_main";
    }

    @GetMapping("/my-project")
    public String myProject(@RequestParam("user_id") String user_id, Model model){
        logger.debug("MainController :: myProject :: {}",user_id);

        List<Project> projectList = mainService.getMyProjectList(user_id);
        List<Folder> folderList = mainService.getMyFolderList(user_id);
        List<Folder_Project> folderProjectList = mainService.getAllFolderProjectList(user_id);

        for(Folder folder : folderList){
            List<String> projectIds = folderProjectList.stream()
                    .filter(fp -> fp.getFolder_id() == folder.getId()) // folder_id 일치 확인
                    .map(fp -> String.valueOf(fp.getProject_id()))     // project_id를 String으로 변환
                    .collect(Collectors.toList());                    // List로 수집
            folder.setFolder_project(projectIds);
        }
        model.addAttribute("projectList",projectList);
        model.addAttribute("folderList", folderList);
        return "main/project";
    }

    @GetMapping("/trash")
    public String trash(@RequestParam("user_id") String user_id,Model model){
        logger.debug("MainController :: trash :: {}",user_id);

        List<Project> trashProjectList = mainService.getMyTrashProjectList(user_id);
        model.addAttribute("trashProjectList",trashProjectList);

        return "main/trash";
    }

    @GetMapping("/bookmark")
    public String bookMark(@RequestParam("user_id") String user_id,Model model){
        logger.debug("MainController :: bookMark :: {}",user_id);

        List<Project> trashProjectList = mainService.getMyTrashProjectList(user_id);
        model.addAttribute("trashProjectList",trashProjectList);

        return "main/trash";
    }

    @GetMapping("/favorite")
    public String favorite(@RequestParam("user_id") String user_id,Model model){
        logger.debug("MainController :: favorite :: {}",user_id);

        List<Project> favoriteProjectList = mainService.getMyFavoriteProjectList(user_id);
        List<Folder> folderList = mainService.getMyFolderList(user_id);
        List<Folder_Project> folderProjectList = mainService.getAllFolderProjectList(user_id);

        for(Folder folder : folderList){
            List<String> projectIds = folderProjectList.stream()
                    .filter(fp -> fp.getFolder_id() == folder.getId()) // folder_id 일치 확인
                    .map(fp -> String.valueOf(fp.getProject_id()))     // project_id를 String으로 변환
                    .collect(Collectors.toList());                    // List로 수집
            folder.setFolder_project(projectIds);
        }
        model.addAttribute("favoriteProjectList",favoriteProjectList);
        model.addAttribute("folderList", folderList);
        return "main/favorite";
    }

    @GetMapping("/folder-project")
    public String folder_project(@RequestParam("folder_id") int folder_id,
                                 @RequestParam("folder_name") String folder_name,
                                 @RequestParam("user_id") String user_id,
                                 Model model){
        logger.debug("MainController :: folder_project :: {}, {},{}",folder_id, folder_name,user_id);

        List<Project> projectList = mainService.getMyFolderProjectList(folder_id);
        //List<Folder_Project> folderProjectList = mainService.getAllFolderProjectList(user_id);
        //List<Folder> folderList = mainService.getMyFolderList(user_id);

        model.addAttribute("folderId",folder_id);
        model.addAttribute("folderName",folder_name);
        model.addAttribute("projectList",projectList);
        //model.addAttribute("folderList", folderList);
        //model.addAttribute("folderProjectList",folderProjectList);

        return "main/project_folder";
    }

    @GetMapping("/theme")
    public String theme(Model model){
        logger.debug("MainController :: theme = /main/theme");
        List<Theme> themeList = mainService.getThemeList();
        model.addAttribute("themeList",themeList);
        return "main/theme";
    }

    @GetMapping("/content")
    public String content(Model model){
        logger.debug("MainController :: content = /main/content");
        List<Content> contentList = mainService.getContentList();
        model.addAttribute("contentList",contentList);
        return "main/content";
    }

    @GetMapping("/unity")
    public String unity(@RequestParam("project_id")int project_id, Model model){
        logger.debug("MainController :: unity = /main/unity");
        Project project = mainService.getProject(project_id);
        List<Quiz_Item> quizItemList = quizService.getAllQuizItemList(project_id);

        Map<String, Object> response = new HashMap<>();
        response.put("project", project);
        response.put("quizItemList", quizItemList);
        model.addAttribute("project_data",response);
        return "main/unity_container";
    }

    @GetMapping("/setting")
    public String setting(@RequestParam("user_id") String user_id, Model model){
        logger.debug("MainController :: setting = /main/setting");

        User user = userService.findByUserId(user_id);

        model.addAttribute("user",user);

        return "main/setting";
    }

    @GetMapping("/account-delete")
    public String accountDelete(Model model){
        logger.debug("MainController :: accountDelete = /main/account-delete");
        return "main/account_delete";
    }

    @PostMapping("/new/project")
    public ResponseEntity<Map<String, Object>> createProject(Project_Object project) {
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: createProject :: {}", project);
        try {
            Project tem = new Project();
            tem.setUser_id(project.getUser_id());
            tem.setName(project.getName());
            tem.setTheme(project.getTheme());
            tem.setContent(project.getContent());
            tem.setMax_participants(project.getMax_participants());
            mainService.createProject(tem);
            logger.debug("project id = {}",tem.getId());
            if(tem.getId() != -1){
                response.put("message", "폴더 생성 성공");
                response.put("project_id",tem.getId());
                response.put("project_name",tem.getName());
                return ResponseEntity.ok(response); // 성공 응답
            }else{
                response.put("message", "폴더 생성 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/max_participants")
    public ResponseEntity<Map<String, Object>> changeMaxParticipants(@RequestParam("project_id")int project_id, @RequestParam("max_participants")int max_participants) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeMaxParticipants :: {},{}", project_id,max_participants);
        try {
            result = mainService.changeMaxParticipants(project_id,max_participants);
            if (result) {
                response.put("message", "참여인원 변경 성공");
                response.put("project_id",project_id);
                response.put("max_participants", max_participants); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "참여인원 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
//        return result ? new ResponseEntity<>("참여인원 변경 성공", HttpStatus.OK) : new ResponseEntity<>("프로젝트 추가 실패", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping("/project/favorite")
    public ResponseEntity<Map<String, Object>> changeIsFavorite(@RequestParam("project_id")int project_id, @RequestParam("is_favorite")int is_favorite) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeIsFavorite :: {},{}", project_id,is_favorite);
        try {
            result = mainService.changeProjectFavorite(project_id,is_favorite);
            if (result) {
                response.put("message", "즐겨찾기 변경 성공");
                response.put("project_id",project_id);
                response.put("is_favorite", is_favorite); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "즐겨찾기 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/folder")
    public ResponseEntity<Map<String, Object>> changeFolderProject(@RequestParam("project_id")int project_id,
                                                                   @RequestParam("folder_id")int folder_id,
                                                                   @RequestParam("is_selected")boolean is_selected) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeIsFavorite :: {},{},{}", project_id,folder_id,is_selected);
        try {
            if(is_selected){
                result = mainService.deleteMyFolderProject(folder_id,project_id);
            }else{
                result = mainService.createMyFolderProject(folder_id,project_id);
            }

            if (result) {
                response.put("message", "폴더 프로젝트 변경 성공");
                response.put("project_id",project_id);
                response.put("folder_id",folder_id);
                response.put("is_selected", !is_selected); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "즐겨찾기 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/deleted")
    public ResponseEntity<Map<String, Object>> changeIsDeleted(@RequestParam("project_id")int project_id, @RequestParam("is_deleted")int is_deleted) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeIsDeleted :: {},{}", project_id,is_deleted);
        try {
            result = mainService.changeProjectDeleted(project_id,is_deleted);
            if (result) {
                response.put("message", "삭제 상태 변경 성공");
                response.put("project_id",project_id);
                response.put("is_deleted", is_deleted); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "삭제 상태 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/permanent-deleted")
    public ResponseEntity<Map<String, Object>> permanentDelete(@RequestParam("project_id")int project_id) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: permanentDelete :: {}", project_id);
        try {
            result = mainService.projectPermanentDelete(project_id);
            if (result) {
                List<Quiz_Item> quizItemList = quizService.getAllQuizItemList(project_id);
                for(Quiz_Item quiz : quizItemList){
                    try{
                        firebaseStorageService.deleteFile(quiz.getImage_url());
                    }catch(Exception e){
                        logger.error("error :: permanentDelete :: deleteFile");
                    }

                }

                response.put("message", "완전 삭제 성공");
                response.put("project_id",project_id);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "완전 삭제 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/new-folder")
    public ResponseEntity<Map<String, Object>> newFolder(@RequestParam("user_id")String user_id, @RequestParam("folder_name")String folder_name) {


        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: newFolder :: {} , {}", user_id,folder_name);
        try {
            Folder folder = new Folder();
            folder.setUser_id(user_id);
            folder.setName(folder_name);
             mainService.createMyFolder(folder);
             logger.debug("folder id = {}",folder.getId());
            if (folder.getId() != -1) {
                response.put("message", "폴더 생성 성공");
                response.put("folder_id",folder.getId());
                response.put("folder_name",folder_name);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "폴더 생성 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/delete-folder")
    public ResponseEntity<Map<String, Object>> deleteFolder(@RequestParam("user_id")String user_id, @RequestParam("folder_id")int folder_id) {
        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: deleteFolder :: {} , {}", user_id,folder_id);
        try {

            result = mainService.deleteMyFolder(user_id,folder_id);

            if (result) {
                response.put("message", "폴더 삭제 성공");
                response.put("folder_id",folder_id);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "폴더 삭제 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/change-folder-name")
    public ResponseEntity<Map<String, Object>> changeFolderName(@RequestParam("folder_id")int folder_id, @RequestParam("folder_name")String folder_name) {
        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeFolderName :: {} , {}", folder_id,folder_name);
        try {

            result = mainService.changeFolderName(folder_id,folder_name);

            if (result) {
                response.put("message", "폴더 이름 변경 성공");
                response.put("folder_id",folder_id);
                response.put("folder_name",folder_name);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "폴더 이름 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/project/change-project-name")
    public ResponseEntity<Map<String, Object>> changeProjectName(@RequestParam("project_id")int project_id, @RequestParam("project_name")String project_name) {
        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeProjectName :: {} , {}", project_id,project_name);
        try {

            result = mainService.changeProjectName(project_id,project_name);

            if (result) {
                response.put("message", "프로젝트 이름 변경 성공");
                response.put("project_id",project_id);
                response.put("project_name",project_name);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "프로젝트 이름 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/change-user-name")
    public ResponseEntity<Map<String, Object>> changeUserName(@RequestParam("user_id")String user_id, @RequestParam("user_name")String user_name) {
        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: changeUserName :: {} , {}", user_id,user_name);
        try {

            result = userService.updateUserName(user_id,user_name);

            if (result) {
                response.put("message", "사용자 이름 변경 성공");
                response.put("user_name",user_name);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "사용자 이름 변경 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete-user")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam("user_id")String user_id) {
        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("MainController :: deleteUser :: {}", user_id);
        try {

            result = userService.deleteUser(user_id);

            if (result) {
                response.put("message", "사용자 삭제 성공");
                response.put("user_id",user_id);
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "사용자 삭제 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<String> change_password(String user_id,String current_password, String change_password){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: change_password :: user_id = {}, change_password = {}",
                user_id,
                change_password);
        try {
            User user = userService.findByUserId(user_id);
            if(user != null){
                if(user.getPassword() == bCryptPasswordEncoder.encode(current_password)){
                    result = userService.updatePassword(user_id,bCryptPasswordEncoder.encode(change_password));
                    if(!result){
                        message = "패스워드 변경간 문제가 발생 하였습니다.";
                    }
                }else{
                    result = false;
                    message = "현재 비밀번호가 일치 하지 않습니다.";
                }
            }else{
                result = false;
                message = "해당 아이디의 계정이 없습니다.";
            }
            //result = crudService.deleteManager(manager);
        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("비밀번호 변경 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/change-name")
    public ResponseEntity<String> change_name(String user_id,String user_name){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: change_password :: user_id = {}, user_name = {}",
                user_id,
                user_name);
        try {
            
            result = userService.updateUserName(user_id,user_name);
            if(!result){
                message = "사용자 이름 변경 실패";
            }

        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("사용자 이름 변경 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @PostMapping("/change-favorite")
    public ResponseEntity<String> changeProjectFavorite(int project_id, int is_favorite){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: changeProjectFavorite :: project_id = {}, is_favorite = {}",
                project_id,
                is_favorite);
        try {
            result = mainService.changeProjectFavorite(project_id,is_favorite);
            if(!result){
                message = "프로젝트 즐겨찾기 수정 실패";
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("프로젝트 즐겨찾기 수정 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/recovery-project")
    public ResponseEntity<String> recoveryProject(int project_id){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: recoveryProject :: project_id = {}", project_id);
        try {
            result = mainService.recoveryProject(project_id);
            if(!result){
                message = "프로젝트 복원 실패";
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("프로젝트 복원 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/temp-deleted-project")
    public ResponseEntity<String> tempDeletedProject(int project_id){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: tempDeletedProject :: project_id = {}", project_id);
        try {
            result = mainService.tempDeletedProject(project_id);
            if(!result){
                message = "프로젝트 휴지통 이동 실패";
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("프로젝트 휴지통 이동 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/remove-project")
    public ResponseEntity<String> removeProject(int project_id){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: removeProject :: project_id = {}", project_id);
        try {
            result = mainService.removeProject(project_id);
            if(!result){
                message = "프로젝트 완전 삭제 실패";
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>("프로젝트 완전 삭제 성공", HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/project-url")
    public ResponseEntity<String> project_url(int project_id){
        boolean result = false;
        String message ="";
        logger.debug("MainController :: project_url :: project_id = {}", project_id);
        try {
            String serverUrl = mainService.getProject(project_id).getServer_url();
            if(serverUrl.length() != 0){
                result = true;
                message = serverUrl;
            }else{
                message = "프로젝트 파일을 생성 중 입니다.";
            }

        } catch (Exception e) {
            message = e.getMessage();
        }
        return result ? new ResponseEntity<>(message, HttpStatus.OK) : new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
