package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for audit logging
 * Business Rule: FR20 - Log all critical actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id")
    private Long userId; // User who performed the action

    @Column(name = "username", length = 255)
    private String username;

    @NotNull
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType; // LOGIN, LOGOUT, CREATE_EVENT, UPDATE_USER, APPROVE_REFUND, etc.

    @Column(name = "entity_type", length = 100)
    private String entityType; // USER, EVENT, ORDER, REFUND, etc.

    @Column(name = "entity_id")
    private Long entityId; // ID of affected entity

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "result", length = 50)
    private String result; // SUCCESS, FAILURE, ERROR

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // For update operations

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // For update operations

    // Constructor for basic audit log
    public AuditLog(Long userId, String username, String actionType, String entityType, Long entityId, String description) {
        this.userId = userId;
        this.username = username;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.result = "SUCCESS";
    }

    // Constructor with IP address
    public AuditLog(Long userId, String username, String actionType, String entityType, Long entityId, 
                   String description, String ipAddress) {
        this(userId, username, actionType, entityType, entityId, description);
        this.ipAddress = ipAddress;
    }

    // Static factory methods for common actions
    public static AuditLog createLoginLog(Long userId, String username, String ipAddress) {
        return new AuditLog(userId, username, "LOGIN", "USER", userId, "User logged in", ipAddress);
    }

    public static AuditLog createEventCreationLog(Long userId, String username, Long eventId) {
        return new AuditLog(userId, username, "CREATE_EVENT", "EVENT", eventId, "Event created");
    }

    public static AuditLog createRefundApprovalLog(Long adminId, String adminName, Long refundId) {
        return new AuditLog(adminId, adminName, "APPROVE_REFUND", "REFUND", refundId, "Refund approved by admin");
    }

    public static AuditLog createKycApprovalLog(Long adminId, String adminName, Long organizerId) {
        return new AuditLog(adminId, adminName, "APPROVE_KYC", "ORGANIZER_PROFILE", organizerId, "KYC verification approved");
    }
}

