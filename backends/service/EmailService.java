package com.hospital.smartpms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Simple email service - in production, you would integrate with
    // an actual email service like SendGrid, AWS SES, etc.

    public void sendEmail(String to, String subject, String body) {
        // For now, just log the email (in production, implement actual email sending)
        logger.info("=== EMAIL SENT ===");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Body: {}", body);
        logger.info("==================");

        // In production, you would implement actual email sending here:
        // - Configure SMTP settings
        // - Use JavaMailSender or external email service
        // - Handle retries and failures
        // - Track delivery status
    }

    public void sendEmailAsync(String to, String subject, String body) {
        // For async email sending
        new Thread(() -> sendEmail(to, subject, body)).start();
    }
}