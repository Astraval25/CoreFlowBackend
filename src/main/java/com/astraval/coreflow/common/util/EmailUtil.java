package com.astraval.coreflow.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendTextEmail(String toEmail, String subject, String bodyText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(bodyText);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to " + toEmail + ": " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public boolean sendHtmlEmail(String toEmail, String subject, String bodyHtml) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(bodyHtml);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to " + toEmail + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}