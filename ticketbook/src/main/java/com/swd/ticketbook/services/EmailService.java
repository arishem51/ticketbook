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
     * Send order confirmation with tickets (UC-02.1)
     * 
     * @param email Recipient email
     * @param orderId Order ID
     * @param eventName Event name
     * @param totalAmount Total amount paid
     * @param tickets List of tickets
     */
    public void sendOrderConfirmation(String email, String orderId, String eventName, 
                                     java.math.BigDecimal totalAmount, java.util.List<?> tickets) {
        logger.info("Sending order confirmation to: {} for order: {}", email, orderId);
        
        String subject = "Ticket Book - Order Confirmation #" + orderId;
        String message = String.format(
            "Thank you for your order!\n\n" +
            "Order ID: %s\n" +
            "Event: %s\n" +
            "Total Amount: $%s\n" +
            "Number of Tickets: %d\n\n" +
            "Your e-tickets are attached to this email.\n" +
            "Please present the QR codes at the event entrance.\n\n" +
            "Enjoy the event!",
            orderId, eventName, totalAmount.toString(), tickets.size()
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send refund request confirmation (UC-02.5)
     * 
     * @param email Customer email
     * @param requestId Refund request ID
     * @param eventName Event name
     * @param refundAmount Refund amount
     */
    public void sendRefundRequestConfirmation(String email, Long requestId, String eventName, 
                                            java.math.BigDecimal refundAmount) {
        logger.info("Sending refund request confirmation to: {} for request: {}", email, requestId);
        
        String subject = "Ticket Book - Refund Request Received #" + requestId;
        String message = String.format(
            "Your refund request has been received.\n\n" +
            "Request ID: %d\n" +
            "Event: %s\n" +
            "Refund Amount: $%s\n\n" +
            "Admin will review your request within 24-48 hours.\n" +
            "You'll receive a notification once the decision is made.",
            requestId, eventName, refundAmount.toString()
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send refund completed notification (UC-02.5)
     * 
     * @param email Customer email
     * @param requestId Refund request ID
     * @param refundAmount Refund amount
     */
    public void sendRefundCompleted(String email, Long requestId, java.math.BigDecimal refundAmount) {
        logger.info("Sending refund completed notification to: {} for request: {}", email, requestId);
        
        String subject = "Ticket Book - Refund Completed #" + requestId;
        String message = String.format(
            "Your refund has been processed successfully.\n\n" +
            "Request ID: %d\n" +
            "Refund Amount: $%s\n\n" +
            "The amount will be credited to your original payment method within 3-5 business days.",
            requestId, refundAmount.toString()
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send refund rejected notification (UC-02.5)
     * 
     * @param email Customer email
     * @param requestId Refund request ID
     * @param reason Rejection reason
     */
    public void sendRefundRejected(String email, Long requestId, String reason) {
        logger.info("Sending refund rejection to: {} for request: {}", email, requestId);
        
        String subject = "Ticket Book - Refund Request Rejected #" + requestId;
        String message = String.format(
            "Your refund request has been reviewed and unfortunately cannot be approved.\n\n" +
            "Request ID: %d\n" +
            "Reason: %s\n\n" +
            "If you have questions, please contact support.",
            requestId, reason
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send new support ticket notification to organizer (UC-02.4)
     * 
     * @param email Organizer email
     * @param ticketId Support ticket ID
     * @param eventName Event name
     * @param subject Ticket subject
     * @param customerName Customer name
     */
    public void sendNewSupportTicketNotification(String email, Long ticketId, String eventName,
                                                String subject, String customerName) {
        logger.info("Sending new support ticket notification to organizer: {}", email);
        
        String emailSubject = "Ticket Book - New Support Request #" + ticketId;
        String message = String.format(
            "You have received a new support request for your event.\n\n" +
            "Ticket ID: %d\n" +
            "Event: %s\n" +
            "From: %s\n" +
            "Subject: %s\n\n" +
            "Please respond to this request within 24-48 hours.",
            ticketId, eventName, customerName, subject
        );
        
        sendEmail(email, emailSubject, message);
    }

    /**
     * Send support request confirmation to customer (UC-02.4)
     * 
     * @param email Customer email
     * @param ticketId Support ticket ID
     * @param eventName Event name
     */
    public void sendSupportRequestConfirmation(String email, Long ticketId, String eventName) {
        logger.info("Sending support request confirmation to customer: {}", email);
        
        String subject = "Ticket Book - Support Request Received #" + ticketId;
        String message = String.format(
            "Your support request has been submitted successfully.\n\n" +
            "Ticket ID: %d\n" +
            "Event: %s\n\n" +
            "The event organizer will respond within 24-48 hours.",
            ticketId, eventName
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send support response notification to customer (UC-02.4)
     * 
     * @param email Customer email
     * @param ticketId Support ticket ID
     * @param eventName Event name
     * @param response Organizer's response
     */
    public void sendSupportResponseNotification(String email, Long ticketId, String eventName,
                                              String response) {
        logger.info("Sending support response notification to customer: {}", email);
        
        String subject = "Ticket Book - Response to Support Request #" + ticketId;
        String message = String.format(
            "The event organizer has responded to your support request.\n\n" +
            "Ticket ID: %d\n" +
            "Event: %s\n\n" +
            "Response:\n%s",
            ticketId, eventName, response
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send support resolved notification (UC-02.4)
     * 
     * @param email Customer email
     * @param ticketId Support ticket ID
     * @param eventName Event name
     */
    public void sendSupportResolvedNotification(String email, Long ticketId, String eventName) {
        logger.info("Sending support resolved notification to customer: {}", email);
        
        String subject = "Ticket Book - Support Request Resolved #" + ticketId;
        String message = String.format(
            "Your support request has been marked as resolved.\n\n" +
            "Ticket ID: %d\n" +
            "Event: %s\n\n" +
            "If your issue has been resolved, please confirm to close the ticket.\n" +
            "Otherwise, you can reopen it by adding a follow-up comment.",
            ticketId, eventName
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send event approval notification to organizer (UC-04.9)
     * 
     * @param email Organizer email
     * @param eventName Event name
     * @param approved Whether event was approved
     * @param adminNotes Admin notes/reason
     */
    public void sendEventApprovalNotification(String email, String eventName, boolean approved, 
                                            String adminNotes) {
        logger.info("Sending event approval notification to organizer: {} - Approved: {}", email, approved);
        
        String subject = approved ? 
            "Ticket Book - Event Approved: " + eventName :
            "Ticket Book - Event Rejected: " + eventName;
            
        String message;
        if (approved) {
            message = String.format(
                "Congratulations! Your event has been approved.\n\n" +
                "Event: %s\n\n" +
                "Admin Notes: %s\n\n" +
                "Your event is now live and customers can start booking tickets.\n" +
                "You can manage your event from the Organizer Dashboard.",
                eventName, adminNotes
            );
        } else {
            message = String.format(
                "Your event submission has been reviewed.\n\n" +
                "Event: %s\n" +
                "Status: Rejected\n\n" +
                "Reason: %s\n\n" +
                "Please review the feedback and resubmit if you'd like to make changes.",
                eventName, adminNotes
            );
        }
        
        sendEmail(email, subject, message);
    }

    /**
     * Send KYC submission confirmation
     * 
     * @param email User email
     * @param fullName User full name
     */
    public void sendKycSubmissionConfirmation(String email, String fullName) {
        logger.info("Sending KYC submission confirmation to: {}", email);
        
        String subject = "Ticket Book - KYC Verification Submitted";
        String message = String.format(
            "Hello %s,\n\n" +
            "Your KYC verification documents have been submitted successfully.\n\n" +
            "Our admin team will review your application within 3-5 business days.\n" +
            "You'll receive an email notification once your verification is processed.\n\n" +
            "Thank you for your patience!",
            fullName
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send KYC approval notification to user (UC-04 KYC)
     * 
     * @param email User email
     * @param fullName User full name
     * @param approved Whether KYC was approved
     * @param notes Admin notes/reason
     */
    public void sendKycApprovalNotification(String email, String fullName, boolean approved, 
                                          String notes) {
        logger.info("Sending KYC approval notification to: {} - Approved: {}", email, approved);
        
        String subject = approved ? 
            "Ticket Book - KYC Verification Approved" :
            "Ticket Book - KYC Verification Rejected";
            
        String message;
        if (approved) {
            message = String.format(
                "Hello %s,\n\n" +
                "Congratulations! Your KYC verification has been approved.\n\n" +
                "Admin Notes: %s\n\n" +
                "You are now a Verified Organizer and can create events.\n" +
                "Visit the Organizer Dashboard to get started.\n\n" +
                "Thank you for choosing Ticket Book!",
                fullName, notes
            );
        } else {
            message = String.format(
                "Hello %s,\n\n" +
                "Your KYC verification has been reviewed.\n" +
                "Status: Not Approved\n\n" +
                "Reason: %s\n\n" +
                "Please review the feedback and resubmit with correct documentation.\n" +
                "If you have questions, please contact support.",
                fullName, notes
            );
        }
        
        sendEmail(email, subject, message);
    }

    /**
     * Send event update request decision
     * 
     * @param email Organizer email
     * @param fullName Organizer full name
     * @param eventName Event name
     * @param approved Whether request was approved
     * @param adminNotes Admin notes/reason
     */
    public void sendEventUpdateRequestDecision(String email, String fullName, String eventName, 
                                              boolean approved, String adminNotes) {
        logger.info("Sending event update decision to: {} - Approved: {}", email, approved);
        
        String subject = approved ? 
            "Ticket Book - Event Update Approved" :
            "Ticket Book - Event Update Rejected";
            
        String message;
        if (approved) {
            message = String.format(
                "Hello %s,\n\n" +
                "Your update request for event '%s' has been approved.\n\n" +
                "Admin Notes: %s\n\n" +
                "The changes have been applied to your event.\n" +
                "You can view the updated event in your Organizer Dashboard.",
                fullName, eventName, adminNotes
            );
        } else {
            message = String.format(
                "Hello %s,\n\n" +
                "Your update request for event '%s' has been reviewed.\n" +
                "Status: Not Approved\n\n" +
                "Reason: %s\n\n" +
                "If you have questions, please contact support.",
                fullName, eventName, adminNotes
            );
        }
        
        sendEmail(email, subject, message);
    }

    /**
     * Send withdrawal request confirmation
     * 
     * @param email Organizer email
     * @param fullName Organizer full name
     * @param amount Withdrawal amount
     */
    public void sendWithdrawalRequestConfirmation(String email, String fullName, java.math.BigDecimal amount) {
        logger.info("Sending withdrawal request confirmation to: {}", email);
        
        String subject = "Ticket Book - Withdrawal Request Received";
        String message = String.format(
            "Hello %s,\n\n" +
            "Your withdrawal request has been submitted successfully.\n\n" +
            "Amount: $%s\n\n" +
            "Our admin team will review your request within 1-3 business days.\n" +
            "The funds will be transferred to your registered bank account after approval.\n\n" +
            "Thank you!",
            fullName, amount.toString()
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send withdrawal approved notification
     * 
     * @param email Organizer email
     * @param fullName Organizer full name
     * @param amount Withdrawal amount
     * @param transactionRef Transaction reference
     */
    public void sendWithdrawalApproved(String email, String fullName, java.math.BigDecimal amount, 
                                      String transactionRef) {
        logger.info("Sending withdrawal approved notification to: {}", email);
        
        String subject = "Ticket Book - Withdrawal Approved";
        String message = String.format(
            "Hello %s,\n\n" +
            "Your withdrawal request has been approved and processed.\n\n" +
            "Amount: $%s\n" +
            "Transaction Reference: %s\n\n" +
            "The funds will be transferred to your registered bank account within 1-3 business days.\n\n" +
            "Thank you!",
            fullName, amount.toString(), transactionRef
        );
        
        sendEmail(email, subject, message);
    }

    /**
     * Send withdrawal rejected notification
     * 
     * @param email Organizer email
     * @param fullName Organizer full name
     * @param amount Withdrawal amount
     * @param reason Rejection reason
     */
    public void sendWithdrawalRejected(String email, String fullName, java.math.BigDecimal amount, 
                                      String reason) {
        logger.info("Sending withdrawal rejection to: {}", email);
        
        String subject = "Ticket Book - Withdrawal Request Rejected";
        String message = String.format(
            "Hello %s,\n\n" +
            "Your withdrawal request has been reviewed.\n" +
            "Status: Not Approved\n\n" +
            "Amount: $%s\n" +
            "Reason: %s\n\n" +
            "If you have questions, please contact support.",
            fullName, amount.toString(), reason
        );
        
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

