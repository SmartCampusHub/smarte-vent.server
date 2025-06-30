package com.winnguyen1905.activity.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

  private final JavaMailSender mailSender;
  
  @Value("${spring.mail.username}")
  private String fromEmail;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(String to, String subject, String body) {
    try {
      log.info("Attempting to send email to: {}", to);
      
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      message.setFrom(fromEmail);

      mailSender.send(message);
      
      log.info("Email sent successfully to: {}", to);
    } catch (Exception e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
      throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
    }
  }
}
