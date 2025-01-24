package com.metamate.metamate_service.controller;

import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Project;
import com.metamate.metamate_service.dto.Quiz_Item;
import com.metamate.metamate_service.dto.Receive_Ai_Quiz_Item;
import com.metamate.metamate_service.service.FirebaseStorageService;
import com.metamate.metamate_service.service.MainService;
import com.metamate.metamate_service.service.QuizService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quiz")
public class QuizController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    MainService mainService;

    @Autowired
    QuizService quizService;

    @Value("${file-upload.path.windows}")
    private String windowsUploadPath;

    @Value("${file-upload.path.linux}")
    private String linuxUploadPath;

    @Autowired
    FirebaseStorageService firebaseStorageService;

//    @PostConstruct
//    public void init() {
//        System.out.println("Windows Upload Path: " + windowsUploadPath);
//        System.out.println("Linux Upload Path: " + linuxUploadPath);
//    }
//
//    private String getUploadDirectory() {
//        String os = System.getProperty("os.name").toLowerCase();
//        return os.contains("win") ? windowsUploadPath : linuxUploadPath;
//    }

    @GetMapping("/main")
    public String quiz(@RequestParam("project_id") int project_id,
                       Model model){
        logger.debug("QuizController :: quiz = quiz/main");


        Project project = mainService.getProject(project_id);

        model.addAttribute("project",project);
        model.addAttribute("project_id",project_id);
        return "quiz/create_quiz_main";
    }

    @GetMapping("/main/page")
    public String quiz_page(@RequestParam("project_id") int project_id,
                            @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
                       Model model){
        logger.debug("QuizController :: quiz_page = quiz/main/page :: {},{}",project_id,currentPage);
        int quizCount = quizService.getQuizItemCount(project_id);
        int limit = 10;  // 한 페이지당 10개 표시
        int paginationSize = 5;  // 페이지 네이션에 표시할 페이지 수

        int totalPage = (int) Math.ceil((double) quizCount / limit);
        int startPage = ((currentPage - 1) / paginationSize) * paginationSize + 1;
        int endPage = Math.min(startPage + paginationSize - 1, totalPage);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);


        List<Quiz_Item> quizList = quizService.getQuizItemList(project_id,currentPage);

        model.addAttribute("quizCount",quizCount);

        model.addAttribute("quizList",quizList);
        model.addAttribute("project_id",project_id);

        return "quiz/create_quiz";
    }

    @GetMapping("/edit/{id}")
    public String quizEdit(@PathVariable("id") int projectId, Model model) {
        logger.debug("QuizController :: quizEdit :: projectId = {}", projectId);
        Project project = mainService.getProject(projectId);
        model.addAttribute("project", project);
        return "quiz/create_quiz";
    }

    @GetMapping("/ai-quiz")
    public String quizAi(@RequestParam("project_id") int projectId, Model model) {
        logger.debug("QuizController :: quizAi :: projectId = {}", projectId);
        Ai_Quiz_Meta ai_quiz_meta = quizService.getMyAiQuizMeta(projectId);

        model.addAttribute("project_id", projectId);
        model.addAttribute("ai_quiz_meta",ai_quiz_meta);
        return "quiz/create_ai_quiz";
    }

    @GetMapping("/ai-create")
    public String createQuizAi(@RequestParam("project_id") int projectId, Model model) {
        logger.debug("QuizController :: createQuizAi :: projectId = {}", projectId);
        Project project = mainService.getProject(projectId);
        model.addAttribute("project", project);
        model.addAttribute("project_id", projectId);
        return "quiz/create_ai_quiz2";
    }

    @PostMapping("/add-quiz-item")
    public ResponseEntity<Map<String, Object>> insertQuizItem(
            @RequestParam("currentPage")int currentPage,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Quiz_Item quizItem) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: insertQuizItem :: {},{}", currentPage,quizItem);
        try {

            //String uploadDirectory = getUploadDirectory();

            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + extension;  // 고유한 파일명 생성
                firebaseStorageService.uploadFile(file,uniqueFileName);
//                Path filePath = Paths.get(uploadDirectory + uniqueFileName);
//                Files.createDirectories(filePath.getParent());  // 디렉토리 생성
//                Files.write(filePath, file.getBytes());  // 파일 저장
                quizItem.setImage_url(uniqueFileName);  // DB에 저장될 파일명
            }

            result = quizService.insertQuizItem(quizItem);
            if (result) {
                response.put("message", "퀴즈 추가 성공");
                response.put("currentPage",currentPage);
                response.put("project_id", quizItem.getProject_id()); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "퀴즈 추가 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/edit-quiz-item")
    public ResponseEntity<Map<String, Object>> updateQuizItem(
            @RequestParam("currentPage")int currentPage,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Quiz_Item quizItem) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: updateQuizItem :: {},{}", currentPage,quizItem);
        try {

            //String uploadDirectory = getUploadDirectory();

            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + extension;  // 고유한 파일명 생성
                firebaseStorageService.uploadFile(file,uniqueFileName);
//                Path filePath = Paths.get(uploadDirectory + uniqueFileName);
//                Files.createDirectories(filePath.getParent());  // 디렉토리 생성
//                Files.write(filePath, file.getBytes());  // 파일 저장
                quizItem.setImage_url(uniqueFileName);  // DB에 저장될 파일명
            }

            result = quizService.updateQuizItem(quizItem);
            if (result) {
                response.put("message", "퀴즈 수정 성공");
                response.put("currentPage",currentPage);
                response.put("project_id", quizItem.getProject_id()); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "퀴즈 수정 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete-quiz-item")
    public ResponseEntity<Map<String, Object>> deleteQuizItem(
            @RequestParam("project_id")int project_id,
            @RequestParam("currentPage")int currentPage,
            @RequestParam("item_id")int item_id,
            @RequestParam("file_name")String file_name) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: deleteQuizItem :: {},{},{},{}", project_id,currentPage,item_id,file_name);
        try {
            result = quizService.deleteQuizItem(item_id);
            if (result) {

                //String uploadDirectory = getUploadDirectory();

                try{
                    // 파일 삭제 로직 추가
                    if (file_name != null && !file_name.isEmpty()) {
                        firebaseStorageService.deleteFile(file_name);
                    }
                }catch(Exception e){
                    logger.info("file name error!!");
                }


                response.put("message", "퀴즈 삭제 성공");
                response.put("project_id",project_id);
                response.put("currentPage",currentPage); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "퀴즈 삭제 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add-ai-quiz-meta")
    public ResponseEntity<Map<String, Object>> insertAiQuizMeta(Ai_Quiz_Meta aiQuizMeta) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: insertAiQuizMeta :: {}", aiQuizMeta);
        try {

            result = quizService.insertAiQuizMeta(aiQuizMeta);
            if (result) {
                response.put("message", "AI 퀴즈 메타 추가 성공");
                response.put("project_id", aiQuizMeta.getProject_id()); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "AI 퀴즈 메타 추가 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/update-ai-quiz-meta")
    public ResponseEntity<Map<String, Object>> updateAiQuizMeta(Ai_Quiz_Meta aiQuizMeta) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: updateAiQuizMeta :: {}", aiQuizMeta);
        try {

            result = quizService.updateAiQuizMeta(aiQuizMeta);
            if (result) {
                response.put("message", "AI 퀴즈 메타 수정 성공");
                response.put("project_id", aiQuizMeta.getProject_id()); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "AI 퀴즈 메타 수정 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add-ai-quiz-items")
    public ResponseEntity<Map<String, Object>> insertAiQuizItems(@RequestBody Map<String, Object> requestData) {
        int project_id = (int) requestData.get("project_id");
        List<Map<String, Object>> quizItemsData = (List<Map<String, Object>>) requestData.get("quizItems");

        List<Receive_Ai_Quiz_Item> quizItems = quizItemsData.stream().map(itemData -> {
            Receive_Ai_Quiz_Item item = new Receive_Ai_Quiz_Item();
            item.setQuestion((String) itemData.get("question"));
            item.setOptions((List<String>) itemData.get("options"));
            item.setAnswer((String) itemData.get("answer"));
            item.setExplanations((String) itemData.get("explanations"));
            return item;
        }).collect(Collectors.toList());


        quizItems.forEach(item -> {
            while (item.getOptions().size() < 4) {
                item.getOptions().add(null);  // 옵션 개수가 부족하면 null 추가
            }
        });

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("QuizController :: insertAiQuizItems :: {},{}",project_id, quizItems);
        try {
            result = quizService.insertAiQuizItems(project_id, quizItems);

            if (result) {
                response.put("message", "AI 퀴즈 추가 성공");
                response.put("project_id", project_id); // 변경된 값 포함
                return ResponseEntity.ok(response); // 성공 응답
            } else {
                response.put("message", "AI 퀴즈 추가 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}


