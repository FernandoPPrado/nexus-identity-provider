package com.fernando.emailModule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void emailSender(String toEmail, String code, String type) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(type);
        email.setText(code);
        javaMailSender.send(email);

    }


}
