package com.example.maildemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final JavaMailSender javaMailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Value("${spring.mail.username}")
    private String sender;

    public EmailController(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(
            @RequestParam("recipient") String recipient,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);

            logger.info("Письмо успешно отправлено: {}", recipient);
            return ResponseEntity.ok("Письмо успешно отправлено по адресу: " + recipient);

        } catch (MailException e) {
            logger.error("Ошибка отправления письма для: {}, ошибка: {}", recipient, e.toString(), e);
            String errorMessage = "Ошибка отправки письма: " + e.getMessage();
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().toString());
                errorMessage += " (Причина: " + e.getCause().getMessage() + ")";
            }
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMessage);
        }
        catch (Exception e) {
            logger.error("Ошибка отправки письма: {}", recipient, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Возникла ошибка отправки письма: " + e.getMessage());
        }
    }
}