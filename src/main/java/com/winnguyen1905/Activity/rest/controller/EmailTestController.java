package com.winnguyen1905.activity.rest.controller;

import com.winnguyen1905.activity.rest.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            emailService.sendEmail(
                to, 
                "Test Email - Configuration Working", 
                "This is a test email to verify your email configuration is working correctly!"
            );
            return ResponseEntity.ok("Email sent successfully to: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to send email: " + e.getMessage());
        }
    }
} 
