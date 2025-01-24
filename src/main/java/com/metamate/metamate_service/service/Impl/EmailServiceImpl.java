package com.metamate.metamate_service.service.Impl;

import com.metamate.metamate_service.dto.User;
import com.metamate.metamate_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "momusk01@gmail.com";

    // 랜덤으로 숫자 생성
    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) { // 인증 코드 8자리
            int index = random.nextInt(3); // 0~2까지 랜덤, 랜덤값으로 switch문 실행

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 97)); // 소문자
                case 1 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> key.append(random.nextInt(10)); // 숫자
            }
        }
        return key.toString();
    }

    public MimeMessage createMail(String mail, String number) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("Do 플랫폼 서비스 확인 코드 입니다.");
        String body = "";
        body += "<h3>아래 임시 확인 코드를 이메일 확인 받기 페이지에 입력하세요</h3>";

        body += "<h3>발급코드 : "+number+"</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    @Override
    public String sendVerificationEmail(String sendEmail) throws MessagingException {
        String number = createNumber(); // 랜덤 인증번호 생성



        MimeMessage message = createMail(sendEmail, number); // 메일 생성

        logger.debug("EmailServiceImpl :: sendVerificationEmail :: message = {}",message.getAllHeaderLines());
        try {
            javaMailSender.send(message); // 메일 발송

        } catch (MailException e) {
            e.printStackTrace();

           logger.error("EmailServiceImpl :: sendVerificationEmail :: error :: {}", e.getMessage());
            return null;

        }

        return number; // 생성된 인증번호 반환
    }

    @Override
    public String sendInitPasswordEmail(String to) {
        logger.debug("EmailServiceImpl :: sendInitPasswordEmail :: email = {}",to);
        String newPassword = createNumber(); // 랜덤 비밀번호 생성

        try {

            MimeMessage message = createInitPasswordMail(to, newPassword); // 메일 생성

            javaMailSender.send(message); // 메일 발송

            return newPassword;

        } catch (MessagingException e) {
           logger.error("EmailServiceImpl :: sendInitPasswordEmail :: MessagingException = {}",e.getMessage());
           return null;
        }catch (MailException e){
            logger.error("EmailServiceImpl :: sendInitPasswordEmail :: MailException = {}",e.getMessage());
            return null;
        }

    }

    public MimeMessage createInitPasswordMail(String mail, String password) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(senderEmail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("Do 플랫폼 비밀번호 변경 메일 입니다.");
        String body = "";
        body += "<h2>비밀번호 변경 알림</h2>";
        body += "<h3>비밀번호 변경 메일 입니다.</h3>";
        body += "<h5>임시비밀번호 : "+ password + "</h5>";
        body += "<h3>비밀번호 변경 요청을 한 사람이 본인이 아닌 경우, 보안을 위해 고객 센터 (02)780-2898로</h3>";
        body += "<h3>연락 주시기 바랍니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }


}
