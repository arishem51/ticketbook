package com.swd.ticketbook.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing password reset tokens.
 * Used for UC-01.3: Reset Password functionality.
 * Tokens expire after 15 minutes.
 * Note: Named "FinancialResetToken" in ERD but used for password resets.
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class FinancialResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "token_value", nullable = false, unique = true, length = 255)
    private String tokenValue;

    @NotNull
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Custom constructor
    public FinancialResetToken(User user, String tokenValue) {
        this.user = user;
        this.tokenValue = tokenValue;
        this.createdAt = LocalDateTime.now();
        this.expiryTime = LocalDateTime.now().plusMinutes(15); // 15 minutes expiry
        this.isUsed = false;
    }

    // Business logic methods
    
    /**
     * Check if token is valid (not used and not expired)
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiryTime);
    }

    /**
     * Mark token as used
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
