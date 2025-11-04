package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user session for session-based authentication.
 * Sessions expire after 24 hours of inactivity.
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
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

    // Custom constructor
    public Session(String sessionToken, User user) {
        this.sessionToken = sessionToken;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.isActive = true;
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
