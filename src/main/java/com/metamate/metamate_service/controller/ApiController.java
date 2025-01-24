package com.metamate.metamate_service.controller;

import com.metamate.metamate_service.dto.Project;
import com.metamate.metamate_service.dto.Quiz_Item;
import com.metamate.metamate_service.service.FirebaseStorageService;
import com.metamate.metamate_service.service.MainService;
import com.metamate.metamate_service.service.QuizService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    MainService mainService;
    @Autowired
    QuizService quizService;
    @Autowired
    FirebaseStorageService firebaseStorageService;


   @GetMapping("/{id}")
    public Map<String, Object> GetProjectData(@PathVariable("id") int id) {
       logger.debug("ApiController :: helloSpring :: call");
       Project project = mainService.getProject(id);
       List<Quiz_Item> quizItemList = quizService.getQuizItemList(id,1);

       Map<String, Object> response = new HashMap<>();
       response.put("project", project);
       response.put("quizItemList", quizItemList);

        return response;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileName") String fileName) {
        try {
            logger.debug("ApiController :: downloadFile :: fileName = {}",fileName);
            byte[] content = firebaseStorageService.downloadFile(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

}
