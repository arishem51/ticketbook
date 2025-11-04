package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.auth.*;
import com.swd.ticketbook.entities.FinancialResetToken;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.UserRole;
import com.swd.ticketbook.repositories.FinancialResetTokenRepository;
import com.swd.ticketbook.repositories.UserRepository;
import com.swd.ticketbook.utils.PasswordEncoderUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for authentication operations
 * Implements UC-01: Authentication use cases
 * Business Rules: FR1, FR9, FR10, FR11, FR12, FR13
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private FinancialResetTokenRepository resetTokenRepository;

    /**
     * UC-01.1: Register Account with Email/Phone
     * Business Rules: FR1, FR9, FR10, FR11
     * 
     * @param request Registration request
     * @param httpRequest HTTP request
     * @return Authentication response with session token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Validate input
        if (!request.hasEmailOrPhone()) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        // FR10: Validate password strength
        if (!PasswordEncoderUtil.isPasswordStrong(request.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
        }

        // FR9: Check for duplicate email/phone
        if (request.getEmail() != null && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("This email is already registered. Please login or use a different email.");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("This phone is already registered. Please login or use a different phone.");
        }

        // Verify verification code
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();
        if (!verificationCodeService.verifyCode(identifier, request.getVerificationCode())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        // FR1: Encode password with MD5
        String encodedPassword = PasswordEncoderUtil.encode(request.getPassword());

        // FR11: Create user with CUSTOMER role
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(encodedPassword);
        user.setRole(UserRole.CUSTOMER);
        user.setOauthProvider(request.getEmail() != null ? "EMAIL" : "PHONE");

        // Mark email/phone as verified
        if (request.getEmail() != null) {
            user.setIsEmailVerified(true);
        }
        if (request.getPhone() != null) {
            user.setIsPhoneVerified(true);
        }

        user = userRepository.save(user);

        // Send welcome notification
        if (user.getEmail() != null) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        }
        if (user.getPhone() != null) {
            smsService.sendWelcomeSMS(user.getPhone(), user.getFullName());
        }

        // Create session and auto-login
        String sessionToken = sessionService.createSession(user, httpRequest);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(sessionToken, mapToUserResponse(user), "Registration successful!");
    }

    /**
     * UC-01.1: Send Verification Code
     * Used during registration
     * 
     * @param emailOrPhone Email or phone to send code to
     */
    public void sendVerificationCode(String emailOrPhone) {
        String code = verificationCodeService.createVerificationCode(emailOrPhone);
        
        if (emailOrPhone.contains("@")) {
            emailService.sendVerificationCode(emailOrPhone, code);
        } else {
            smsService.sendVerificationCode(emailOrPhone, code);
        }
    }

    /**
     * UC-01.2: Login with Email/Phone and Password
     * Business Rules: FR1, FR12
     * 
     * @param request Login request
     * @param httpRequest HTTP request
     * @return Authentication response with session token
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        if (!request.hasEmailOrPhone()) {
            throw new IllegalArgumentException("Email or phone number is required");
        }

        // Find user by email or phone
        String identifier = request.getIdentifier();
        Optional<User> userOpt = userRepository.findByEmailOrPhone(identifier);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid phone/email or password");
        }

        User user = userOpt.get();

        // Check if account is deleted
        if (user.getIsDeleted()) {
            throw new IllegalArgumentException("Account not found. Please register a new account.");
        }

        // FR12: Check if account is locked
        if (user.isAccountLocked()) {
            throw new IllegalArgumentException("Account temporarily locked. Please reset your password or try again in 30 minutes");
        }

        // FR1: Verify password using MD5 hash comparison
        if (!PasswordEncoderUtil.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid phone/email or password");
        }

        // Reset failed attempts on successful login
        user.resetFailedLoginAttempts();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Create session
        String sessionToken = sessionService.createSession(user, httpRequest);

        return new AuthResponse(sessionToken, mapToUserResponse(user), "Login successful!");
    }

    /**
     * UC-01.2: Logout
     * Invalidates the current session
     * 
     * @param sessionToken Session token to invalidate
     */
    @Transactional
    public void logout(String sessionToken) {
        sessionService.invalidateSession(sessionToken);
    }

    /**
     * UC-01.3: Request Password Reset
     * Sends reset link via email/SMS
     * 
     * @param emailOrPhone Email or phone to send reset link to
     */
    @Transactional
    public void requestPasswordReset(String emailOrPhone) {
        // Find user (but don't reveal if account exists for security)
        Optional<User> userOpt = userRepository.findByEmailOrPhone(emailOrPhone);
        
        if (userOpt.isEmpty()) {
            // Return success message anyway (security measure)
            return;
        }

        User user = userOpt.get();

        // Invalidate any existing reset tokens
        resetTokenRepository.invalidateAllUserTokens(user);

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        FinancialResetToken token = new FinancialResetToken(user, resetToken);
        resetTokenRepository.save(token);

        // Send reset link
        if (emailOrPhone.contains("@")) {
            emailService.sendPasswordResetLink(emailOrPhone, resetToken);
        } else {
            smsService.sendPasswordResetLink(emailOrPhone, resetToken);
        }
    }

    /**
     * UC-01.3: Reset Password with Token
     * Business Rule: FR10
     * 
     * @param request Reset password request
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        // FR10: Validate password strength
        if (!PasswordEncoderUtil.isPasswordStrong(request.getNewPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
        }

        // Validate token
        Optional<FinancialResetToken> tokenOpt = resetTokenRepository.findValidToken(
            request.getToken(), 
            LocalDateTime.now()
        );

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Reset link expired. Please request a new one.");
        }

        FinancialResetToken token = tokenOpt.get();
        User user = token.getUser();

        // FR1: Encode new password with MD5
        String encodedPassword = PasswordEncoderUtil.encode(request.getNewPassword());
        user.setPassword(encodedPassword);

        // Mark token as used
        token.markAsUsed();

        // Reset failed login attempts
        user.resetFailedLoginAttempts();

        userRepository.save(user);
        resetTokenRepository.save(token);

        // Invalidate all active sessions
        sessionService.invalidateAllUserSessions(user);

        // Send confirmation
        if (user.getEmail() != null) {
            emailService.sendPasswordChangeConfirmation(user.getEmail());
        }
        if (user.getPhone() != null) {
            smsService.sendPasswordChangeConfirmation(user.getPhone());
        }
    }

    /**
     * UC-01.4: View Profile
     * Returns user information
     * 
     * @param userId User ID
     * @return User response
     */
    public UserResponse viewProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getIsDeleted()) {
            throw new IllegalArgumentException("User not found");
        }

        return mapToUserResponse(user);
    }

    /**
     * UC-01.5: Edit Profile
     * Business Rule: FR9
     * 
     * @param userId User ID
     * @param request Update profile request
     * @return Updated user response
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update full name
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        // Update email (requires verification)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // FR9: Check for duplicate email
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException("This contact is already in use");
            }

            // Verify verification code
            if (request.getVerificationCode() == null || 
                !verificationCodeService.verifyCode(request.getEmail(), request.getVerificationCode())) {
                throw new IllegalArgumentException("Invalid verification code");
            }

            user.setEmail(request.getEmail());
            user.setIsEmailVerified(true);
        }

        // Update phone (requires verification)
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // FR9: Check for duplicate phone
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("This contact is already in use");
            }

            // Verify verification code
            if (request.getVerificationCode() == null || 
                !verificationCodeService.verifyCode(request.getPhone(), request.getVerificationCode())) {
                throw new IllegalArgumentException("Invalid verification code");
            }

            user.setPhone(request.getPhone());
            user.setIsPhoneVerified(true);
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    /**
     * UC-01.6: Delete Account (Soft Delete)
     * Business Rule: FR13 - Only for CUSTOMER role
     * 
     * @param userId User ID
     * @param password Current password for confirmation
     */
    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // FR13: Only CUSTOMER can delete their own account
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Organizer and Admin accounts cannot be deleted by users");
        }

        // Verify password
        if (!PasswordEncoderUtil.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password. Deletion cancelled.");
        }

        // Check for pending orders (would be implemented in order service)
        // For now, we'll skip this check

        // Soft delete
        user.softDelete();
        userRepository.save(user);

        // Invalidate all sessions
        sessionService.invalidateAllUserSessions(user);

        // Send confirmation
        if (user.getEmail() != null) {
            emailService.sendAccountDeletionConfirmation(user.getEmail());
        }
    }

    /**
     * UC-01.7: Change Password
     * Business Rules: FR1, FR10, FR20
     * 
     * @param userId User ID
     * @param request Change password request
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if OAuth user without password
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Your account uses Google sign-in and doesn't have a password. To set a password, please use 'Reset Password' from the login page.");
        }

        // Validate passwords match
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        // Verify current password (FR1)
        if (!PasswordEncoderUtil.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect. Please try again.");
        }

        // FR10: Validate new password strength
        if (!PasswordEncoderUtil.isPasswordStrong(request.getNewPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
        }

        // Check new password is different from current
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password");
        }

        // FR1: Encode new password with MD5
        String encodedPassword = PasswordEncoderUtil.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Invalidate all active sessions (FR20)
        sessionService.invalidateAllUserSessions(user);

        // Send confirmation notification
        if (user.getEmail() != null) {
            emailService.sendPasswordChangeConfirmation(user.getEmail());
        }
        if (user.getPhone() != null) {
            smsService.sendPasswordChangeConfirmation(user.getPhone());
        }
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setRegistrationDate(user.getRegistrationDate());
        response.setLastLogin(user.getLastLogin());
        response.setIsEmailVerified(user.getIsEmailVerified());
        response.setIsPhoneVerified(user.getIsPhoneVerified());
        response.setOauthProvider(user.getOauthProvider());
        return response;
    }
}

