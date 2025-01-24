package com.metamate.metamate_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testMailSender(){
        assert mailSender != null : "JavaMailSender Bean is not registered.";
    }
}
