package com.metamate.metamate_service.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    String sendVerificationEmail(String to)throws MessagingException;
    String sendInitPasswordEmail(String to);
}
