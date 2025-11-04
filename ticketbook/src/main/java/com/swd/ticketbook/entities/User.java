package com.swd.ticketbook.entities;

import com.swd.ticketbook.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a User in the system.
 * Supports Customer, Verified Organizer, and Admin roles.
 * Business Rules: FR9, FR10, FR11, FR12, FR13
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "contact", unique = true, length = 255)
    private String contact;

    // Password is MD5 hashed (FR1) - Note: MD5 is deprecated, consider BCrypt
    @Column(name = "password", length = 255)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.CUSTOMER; // FR11: Default role is CUSTOMER

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // Soft delete (FR13)

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // For OAuth users (Google Sign-in)
    @Column(name = "oauth_provider", length = 50)
    private String oauthProvider; // "GOOGLE", "EMAIL", "PHONE"

    @Column(name = "oauth_provider_id", length = 255)
    private String oauthProviderId;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    // Account lockout fields (FR12)
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Session> sessions = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrganizerProfile organizerProfile;

    // Custom constructor
    public User(String fullName, String contact, String password, UserRole role) {
        this.fullName = fullName;
        this.contact = contact;
        this.password = password;
        this.role = role != null ? role : UserRole.CUSTOMER;
        this.registrationDate = LocalDateTime.now();
        this.isDeleted = false;
        this.failedLoginAttempts = 0;
    }

    // Business logic methods
    
    /**
     * Check if account is currently locked (FR12)
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(accountLockedUntil);
    }

    /**
     * Increment failed login attempts and lock if necessary (FR12)
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Reset failed login attempts on successful login
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Soft delete the account (FR13 - only for CUSTOMER role)
     */
    public void softDelete() {
        if (this.role != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only Customer accounts can be self-deleted");
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Check if contact is email (contains @)
     */
    public boolean isEmail() {
        return contact != null && contact.contains("@");
    }

    /**
     * Check if contact is phone (numeric)
     */
    public boolean isPhone() {
        return contact != null && contact.matches("\\d+");
    }

    /**
     * Get email (if contact is email format)
     */
    public String getEmail() {
        return isEmail() ? contact : null;
    }

    /**
     * Get phone (if contact is phone format)
     */
    public String getPhone() {
        return isPhone() ? contact : null;
    }
}
