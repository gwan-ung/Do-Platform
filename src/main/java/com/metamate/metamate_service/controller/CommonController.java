package com.metamate.metamate_service.controller;

import com.metamate.metamate_service.dto.User;
import com.metamate.metamate_service.service.EmailService;
import com.metamate.metamate_service.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@AllArgsConstructor
public class CommonController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/session/keep-alive")
    public ResponseEntity<String> keepAlive(){
        return new ResponseEntity<>("session keep alive refreshing success", HttpStatus.OK);
    }


    @GetMapping({"/", "/index"})
    public String index() {
        logger.debug("CommonController :: index");
        return "index";
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/init-login")
    public String init_login(Model model){
        logger.debug("CommonController :: init_login");
        model.addAttribute("message","");
        return "login/login";
    }
    @GetMapping("/error-login")
    public String error_login(@RequestParam("message") String message, Model model){
        logger.debug("CommonController :: error_login");
        model.addAttribute("message",message);
        return "login/login";
    }

    @GetMapping("/sign-up")
    public String sign_up(){
        logger.debug("CommonController :: sign_up");
        return "login/sign_up";
    }

    @GetMapping("/sign-up-email-confirm")
    public String sign_up_email_confirm(@RequestParam("email") String email, Model model){
        logger.debug("CommonController :: sign_up_email_confirm");
        model.addAttribute("email", email);
        return "login/sign_up_email_confirm";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String code, Model model) {
        logger.debug("CommonController :: verifyUser :: code = {}",code);
        boolean isVerified = userService.verifyUser(code);
        if (isVerified) {
            return "login/login";
        } else {
            model.addAttribute("message","코드가 일치 하지 않습니다.");
            return "login/sign_up_email_confirm";
        }

    }

    @GetMapping("/sign-up-email")
    public String sign_up_email(@RequestParam("email") String email, Model model){
        logger.debug("CommonController :: sign_up_email");
        model.addAttribute("email", email);
        return "login/sign_up_email";
    }
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email, Model model){
        logger.debug("CommonController :: resetPassword");
        User user = userService.findByUserId(email);
        if(user != null){
           boolean result = userService.resetPassword(email);
           if(!result){
               model.addAttribute("message", "임시 비밀번호 발급간 문제가 발생하였습니다.\n 다시 시도해 주세요.");
           }else{
               model.addAttribute("message", "임시 비밀번호를 보내드렸습니다.\n 로그인 화면으로 돌아가셔서 임시 비밀번호를 입력하세요.");
           }
        }else{
            model.addAttribute("message", "사용자가 존재 하지 않습니다.\n 가입하기로 이동하세요.");
        }
        model.addAttribute("email", email);
        return "login/reset_password";
    }

    @GetMapping("/register-user-email")
    public String register_user_email(@RequestParam("email") String email,@RequestParam("password") String password, Model model){
        logger.debug("CommonController :: register_user_email");
        User user = new User();
        user.setUser_id(email);
        user.setName(email);
        user.setLogin_type("SELF");
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setVerified(1);
        user.setRole("ROLE_USER");
        boolean result = userService.registerEmailUser(user);
        if(result){
            model.addAttribute("email", email);
            return "login/login";
        }else{
            model.addAttribute("message","이메일로 가입하기 실패");
            return "login/sign_up_email";
        }
    }

    @GetMapping("/retry-verify-code")
    public String retry_verify_code(@RequestParam("email") String email, Model model){
        logger.debug("CommonController :: retry_verify_code :: {}",email);

        userService.retryVerifyCode(email);

        return "login/sign_up_email_confirm";

    }
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestParam("user_id")String user_id,
                                                              @RequestParam("currentPassword")String currentPassword,
                                                              @RequestParam("newPassword")String newPassword) {

        boolean result = false;
        Map<String, Object> response = new HashMap<>();
        logger.debug("CommonController :: changePassword :: {},{},{}", user_id,currentPassword,newPassword);
        try {
            User user = userService.findByUserId(user_id);
            if(user != null){
                //String encPassword = bCryptPasswordEncoder.encode(currentPassword);
                if(bCryptPasswordEncoder.matches(currentPassword, user.getPassword())){
                    result = userService.updatePassword(user_id,bCryptPasswordEncoder.encode(newPassword));
                    if (result) {
                        response.put("message", "비밀번호 변경 성공");
                        return ResponseEntity.ok(response); // 성공 응답
                    } else {
                        response.put("message", "비밀번호 변경 실패");
                        response.put("error_code", 1);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }else{
                    response.put("message", "비밀번호가 다릅니다.");
                    response.put("error_code", 2);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }else {
                response.put("message", "사용자가 존재하지 않습니다.");
                response.put("error_code", 4);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "서버 오류 발생");
            response.put("error_code", 4);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }
//    @GetMapping("/home")
//    public String home(Model model){
//        logger.debug("CommonController :: home = /home");
//        return "/home";
//    }
//
//    @GetMapping("/error")
//    public String error(Model model){
//        logger.debug("CommonController :: error = /error");
//        return "/error";
//    }


    // 등록 페이지
//    @GetMapping("/register")
//    public String showRegistrationForm(Model model) {
//        logger.debug("CommonController :: showRegistrationForm = /register");
//        model.addAttribute("user", new User());
//        return "register";
//    }
//
//    // 등록 요청 처리
//    @PostMapping("/register-user")
//    public String registerUser(@ModelAttribute("user") User user, Model model) {
//        try {
//            logger.debug("CommonController :: registerUser = /register-user");
//            // 비밀번호 암호화
//            //user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//            // 사용자 등록
//            userService.registerUser(user);
//
//            model.addAttribute("message", "Check Your email.");
//            return "login/sign_up_email_confirm"; // 성공 시 로그인 페이지로 리디렉션
//        } catch (Exception e) {
//            model.addAttribute("error", "Registration failed: " + e.getMessage());
//            return "/login"; // 실패 시 등록 페이지로 리디렉션
//        }
//    }
//

//
//    @GetMapping("/message")
//    public String showMessage(@RequestParam(value = "error", required = false) String error,
//                              @RequestParam(value = "error_message",required = false) String error_message,
//                              Model model) {
//        logger.debug("CommonController :: showMessage :: error = {},error_message= {}",error,error_message);
//        model.addAttribute("error", error);
//        model.addAttribute("message", error_message);
//        return "/message";
//    }
//
//    @GetMapping("/new-password")
//    public String initPassword(@RequestParam("email") String email, Model model) {
//        logger.debug("CommonController :: initPassword :: email = {}",email);
//        boolean isSuccess = false;
//        String password = emailService.sendInitPasswordEmail(email);
//        if(password != null){
//            model.addAttribute("title", "비밀번호 변경 알림");
//            model.addAttribute("message", email+"해당 이메일에서 비밀번호 변경 링크를 클릭하세요");
//            String encodePassword = passwordEncoder.encode(password);
//            isSuccess = userService.updatePassword(email,encodePassword);
//        }
//
//        if(!isSuccess){
//            model.addAttribute("title", "비밀번호 변경 실패 알림");
//            model.addAttribute("message", "비밀번호 초기화 과정에서 문제가 발생 하였습니다. 다시 시도해 주십시요.");
//        }
//
//        return "/message_ex";
//    }

    @GetMapping("/email-login")
    public String emailLogin(@RequestParam("email") String email,@RequestParam("password") String password, Model model) {
        logger.debug("CommonController :: emailLogin :: email = {}",email);
//        boolean isSuccess = false;
//        String password = emailService.sendInitPasswordEmail(email);
//        if(password != null){
//            model.addAttribute("title", "비밀번호 변경 알림");
//            model.addAttribute("message", email+"해당 이메일에서 비밀번호 변경 링크를 클릭하세요");
//            String encodePassword = passwordEncoder.encode(password);
//            isSuccess = userService.updatePassword(email,encodePassword);
//        }
//
//        if(!isSuccess){
//            model.addAttribute("title", "비밀번호 변경 실패 알림");
//            model.addAttribute("message", "비밀번호 초기화 과정에서 문제가 발생 하였습니다. 다시 시도해 주십시요.");
//        }

        return "/home";
    }
}
