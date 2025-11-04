package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing a user session for session-based authentication.
 * Sessions expire after 24 hours of inactivity.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @NotNull
    @Column(name = "session_token", nullable = false, unique = true, length = 255)
    private String sessionToken;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public Session() {
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // 24 hour expiry
    }

    public Session(String sessionToken, User user) {
        this();
        this.sessionToken = sessionToken;
        this.user = user;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Business logic methods
    
    /**
     * Check if session is valid (not expired and active)
     */
    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Update last activity and extend expiration
     */
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    /**
     * Invalidate the session
     */
    public void invalidate() {
        this.isActive = false;
    }
}

