package com.swd.ticketbook.services;

import com.swd.ticketbook.entities.AuditLog;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging
 * Business Rule: FR20 - Log all critical actions
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an action
     */
    @Transactional
    public void logAction(Long userId, String username, String actionType, 
                         String entityType, Long entityId, String description) {
        try {
            AuditLog auditLog = new AuditLog(userId, username, actionType, entityType, entityId, description);
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by user {}", actionType, userId);
        } catch (Exception e) {
            // Don't let logging failures affect business operations
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log an action with IP address
     */
    @Transactional
    public void logAction(User user, String actionType, String entityType, Long entityId, 
                         String description, HttpServletRequest request) {
        try {
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            
            AuditLog auditLog = new AuditLog(
                user.getUserId(), 
                user.getFullName(), 
                actionType, 
                entityType, 
                entityId, 
                description, 
                ipAddress
            );
            auditLog.setUserAgent(userAgent);
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by user {} from IP {}", actionType, user.getUserId(), ipAddress);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log with old/new values for updates
     */
    @Transactional
    public void logUpdate(User user, String entityType, Long entityId, 
                         String description, String oldValue, String newValue) {
        try {
            AuditLog auditLog = new AuditLog(
                user.getUserId(), 
                user.getFullName(), 
                "UPDATE_" + entityType, 
                entityType, 
                entityId, 
                description
            );
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log a failed action
     */
    @Transactional
    public void logFailure(Long userId, String username, String actionType, 
                          String description, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog(userId, username, actionType, null, null, description);
            auditLog.setResult("FAILURE");
            auditLog.setErrorMessage(errorMessage);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findRecentLogs();
    }

    /**
     * Get logs by user
     */
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Get logs by action type
     */
    public List<AuditLog> getLogsByActionType(String actionType) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType);
    }

    /**
     * Get logs by entity
     */
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Get logs within date range
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // Convenience methods for common actions
    
    public void logLogin(User user, HttpServletRequest request) {
        logAction(user, "LOGIN", "USER", user.getUserId(), "User logged in", request);
    }

    public void logLogout(User user) {
        logAction(user.getUserId(), user.getFullName(), "LOGOUT", "USER", user.getUserId(), "User logged out");
    }

    public void logEventCreation(User organizer, Long eventId, String eventName) {
        logAction(organizer.getUserId(), organizer.getFullName(), "CREATE_EVENT", "EVENT", eventId, 
                 "Event created: " + eventName);
    }

    public void logEventUpdate(User user, Long eventId, String description) {
        logAction(user.getUserId(), user.getFullName(), "UPDATE_EVENT", "EVENT", eventId, description);
    }

    public void logEventApproval(User admin, Long eventId, boolean approved) {
        String action = approved ? "APPROVE_EVENT" : "REJECT_EVENT";
        logAction(admin.getUserId(), admin.getFullName(), action, "EVENT", eventId, 
                 "Event " + (approved ? "approved" : "rejected"));
    }

    public void logKycSubmission(User user) {
        logAction(user.getUserId(), user.getFullName(), "SUBMIT_KYC", "ORGANIZER_PROFILE", user.getUserId(), 
                 "KYC verification submitted");
    }

    public void logKycApproval(User admin, Long organizerId, boolean approved) {
        String action = approved ? "APPROVE_KYC" : "REJECT_KYC";
        logAction(admin.getUserId(), admin.getFullName(), action, "ORGANIZER_PROFILE", organizerId, 
                 "KYC " + (approved ? "approved" : "rejected"));
    }

    public void logRefundRequest(User user, Long refundId) {
        logAction(user.getUserId(), user.getFullName(), "REQUEST_REFUND", "REFUND", refundId, 
                 "Refund requested");
    }

    public void logRefundApproval(User admin, Long refundId, boolean approved) {
        String action = approved ? "APPROVE_REFUND" : "REJECT_REFUND";
        logAction(admin.getUserId(), admin.getFullName(), action, "REFUND", refundId, 
                 "Refund " + (approved ? "approved" : "rejected"));
    }

    public void logWithdrawalRequest(User organizer, Long withdrawalId) {
        logAction(organizer.getUserId(), organizer.getFullName(), "REQUEST_WITHDRAWAL", "WITHDRAWAL", withdrawalId, 
                 "Withdrawal requested");
    }

    public void logWithdrawalApproval(User admin, Long withdrawalId, boolean approved) {
        String action = approved ? "APPROVE_WITHDRAWAL" : "REJECT_WITHDRAWAL";
        logAction(admin.getUserId(), admin.getFullName(), action, "WITHDRAWAL", withdrawalId, 
                 "Withdrawal " + (approved ? "approved" : "rejected"));
    }

    public void logOrderCreation(User customer, Long orderId) {
        logAction(customer.getUserId(), customer.getFullName(), "CREATE_ORDER", "ORDER", orderId, 
                 "Order created");
    }

    public void logCheckIn(Long ticketId) {
        logAction(null, "SYSTEM", "CHECK_IN", "TICKET", ticketId, "Ticket checked in");
    }
}

