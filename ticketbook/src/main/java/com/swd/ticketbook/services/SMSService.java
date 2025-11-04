package com.swd.ticketbook.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending SMS messages
 * Used for verification codes and notifications
 */
@Service
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);

    /**
     * Send verification code SMS
     * Used during registration (UC-01.1) and profile update (UC-01.5)
     * 
     * @param phone Recipient phone number
     * @param code Verification code
     */
    public void sendVerificationCode(String phone, String code) {
        // TODO: Implement actual SMS sending using external service (Twilio, AWS SNS, etc.)
        logger.info("Sending verification code {} to phone: {}", code, phone);
        
        String message = String.format(
            "Your Ticket Book verification code is: %s. Valid for 5 minutes.",
            code
        );
        
        sendSMS(phone, message);
    }

    /**
     * Send password reset link via SMS
     * Used during password reset (UC-01.3)
     * 
     * @param phone Recipient phone number
     * @param resetToken Reset token
     */
    public void sendPasswordResetLink(String phone, String resetToken) {
        logger.info("Sending password reset link to phone: {}", phone);
        
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        String message = String.format(
            "Ticket Book: Reset your password using this link: %s (Valid for 15 minutes)",
            resetLink
        );
        
        sendSMS(phone, message);
    }

    /**
     * Send welcome SMS
     * Used after successful registration (UC-01.1)
     * 
     * @param phone Recipient phone number
     * @param fullName User's full name
     */
    public void sendWelcomeSMS(String phone, String fullName) {
        logger.info("Sending welcome SMS to: {}", phone);
        
        String message = String.format(
            "Welcome to Ticket Book, %s! Your account has been created successfully.",
            fullName
        );
        
        sendSMS(phone, message);
    }

    /**
     * Send password change confirmation SMS
     * Used after password change (UC-01.7)
     * 
     * @param phone Recipient phone number
     */
    public void sendPasswordChangeConfirmation(String phone) {
        logger.info("Sending password change confirmation to: {}", phone);
        
        String message = "Ticket Book: Your password has been changed. Contact support if you didn't make this change.";
        
        sendSMS(phone, message);
    }

    /**
     * Generic SMS sending method
     * TODO: Implement actual SMS sending logic
     * 
     * @param phone Recipient phone number
     * @param message SMS content
     */
    private void sendSMS(String phone, String message) {
        // TODO: Implement using Twilio, AWS SNS, or other SMS provider
        // For now, just log the SMS content
        logger.info("SMS Details:\nTo: {}\nMessage: {}", phone, message);
        
        // Example implementation with Twilio:
        /*
        try {
            Message twilioMessage = Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(twilioPhoneNumber),
                message
            ).create();
            
            logger.info("SMS sent successfully to: {}. SID: {}", phone, twilioMessage.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", phone, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
        */
    }
}

