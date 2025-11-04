package com.swd.ticketbook.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending emails
 * Used for verification codes, password reset, and notifications
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Send verification code email
     * Used during registration (UC-01.1) and profile update (UC-01.5)
     * 
     * @param email Recipient email
     * @param code Verification code
     */
    public void sendVerificationCode(String email, String code) {
        // TODO: Implement actual email sending using JavaMailSender or external service
        logger.info("Sending verification code {} to email: {}", code, email);
        
        String subject = "Ticket Book - Email Verification";
        String message = String.format(
            "Your verification code is: %s\n\n" +
            "This code will expire in 5 minutes.\n" +
            "If you didn't request this code, please ignore this email.",
            code
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send password reset link
     * Used during password reset (UC-01.3)
     * 
     * @param email Recipient email
     * @param resetToken Reset token
     */
    public void sendPasswordResetLink(String email, String resetToken) {
        logger.info("Sending password reset link to email: {}", email);
        
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        String subject = "Ticket Book - Password Reset";
        String message = String.format(
            "You requested to reset your password.\n\n" +
            "Click the link below to reset your password:\n%s\n\n" +
            "This link will expire in 15 minutes.\n" +
            "If you didn't request this, please ignore this email.",
            resetLink
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send welcome email
     * Used after successful registration (UC-01.1)
     * 
     * @param email Recipient email
     * @param fullName User's full name
     */
    public void sendWelcomeEmail(String email, String fullName) {
        logger.info("Sending welcome email to: {}", email);
        
        String subject = "Welcome to Ticket Book!";
        String message = String.format(
            "Hello %s,\n\n" +
            "Welcome to Ticket Book! Your account has been successfully created.\n\n" +
            "You can now browse events and book tickets.\n\n" +
            "Thank you for joining us!",
            fullName
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send password change confirmation
     * Used after password change (UC-01.7)
     * 
     * @param email Recipient email
     */
    public void sendPasswordChangeConfirmation(String email) {
        logger.info("Sending password change confirmation to: {}", email);
        
        String subject = "Ticket Book - Password Changed";
        String message = 
            "Your password has been changed successfully.\n\n" +
            "If you didn't make this change, please contact support immediately.";
        
        sendEmail(email, subject, message);
    }

    /**
     * Send account deletion confirmation
     * Used after account deletion (UC-01.6)
     * 
     * @param email Recipient email
     */
    public void sendAccountDeletionConfirmation(String email) {
        logger.info("Sending account deletion confirmation to: {}", email);
        
        String subject = "Ticket Book - Account Deleted";
        String message = 
            "Your account has been successfully deleted.\n\n" +
            "You can recover your account within 30 days by contacting support.\n" +
            "After 90 days, all your data will be permanently deleted.";
        
        sendEmail(email, subject, message);
    }

    /**
     * Generic email sending method
     * TODO: Implement actual email sending logic
     * 
     * @param to Recipient email
     * @param subject Email subject
     * @param message Email body
     */
    private void sendEmail(String to, String subject, String message) {
        // TODO: Implement using Spring Mail or external service (SendGrid, AWS SES, etc.)
        // For now, just log the email content
        logger.info("Email Details:\nTo: {}\nSubject: {}\nMessage: {}", to, subject, message);
        
        // Example implementation with Spring Mail:
        /*
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@ticketbook.com");
            
            mailSender.send(mailMessage);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
        */
    }
}

