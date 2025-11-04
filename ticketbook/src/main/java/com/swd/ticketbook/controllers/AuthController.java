package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.auth.*;
import com.swd.ticketbook.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Implements UC-01: Authentication use cases
 * 
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * UC-01.1: Send Verification Code
     * POST /api/auth/send-verification-code
     * 
     * @param request Contains email or phone
     * @return Success message
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @RequestBody VerificationRequest request) {
        
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();
        authService.sendVerificationCode(identifier);
        
        return ResponseEntity.ok(
            ApiResponse.success("Verification code sent successfully. Valid for 5 minutes.")
        );
    }

    /**
     * UC-01.1: Register Account
     * POST /api/auth/register
     * 
     * @param request Registration details
     * @param httpRequest HTTP request for session creation
     * @return Authentication response with session token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        AuthResponse authResponse = authService.register(request, httpRequest);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(authResponse, "Registration successful!"));
    }

    /**
     * UC-01.2: Login
     * POST /api/auth/login
     * 
     * @param request Login credentials
     * @param httpRequest HTTP request for session creation
     * @return Authentication response with session token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        AuthResponse authResponse = authService.login(request, httpRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(authResponse, "Login successful!")
        );
    }

    /**
     * UC-01.2: Logout
     * POST /api/auth/logout
     * 
     * @param sessionToken Session token from header
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String sessionToken) {
        
        // Remove "Bearer " prefix if present
        if (sessionToken.startsWith("Bearer ")) {
            sessionToken = sessionToken.substring(7);
        }
        
        authService.logout(sessionToken);
        
        return ResponseEntity.ok(
            ApiResponse.success("Logout successful!")
        );
    }

    /**
     * UC-01.3: Request Password Reset
     * POST /api/auth/forgot-password
     * 
     * @param request Contains email or phone
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody VerificationRequest request) {
        
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();
        authService.requestPasswordReset(identifier);
        
        return ResponseEntity.ok(
            ApiResponse.success("If account exists, reset link has been sent. Please check your email/phone.")
        );
    }

    /**
     * UC-01.3: Reset Password with Token
     * POST /api/auth/reset-password
     * 
     * @param request Reset password details including token
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        authService.resetPassword(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Password reset successful! Please login with your new password.")
        );
    }

    /**
     * UC-01.4: View Profile
     * GET /api/auth/profile
     * 
     * @param user Current authenticated user (injected by @CurrentUser)
     * @return User profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @com.swd.ticketbook.security.CurrentUser com.swd.ticketbook.entities.User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        UserResponse profile = authService.viewProfile(user.getUserId());
        
        return ResponseEntity.ok(
            ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * UC-01.5: Edit Profile
     * PUT /api/auth/profile
     * 
     * @param request Update profile details
     * @param user Current authenticated user
     * @return Updated user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @com.swd.ticketbook.security.CurrentUser com.swd.ticketbook.entities.User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        UserResponse updatedProfile = authService.updateProfile(user.getUserId(), request);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedProfile, "Profile updated successfully")
        );
    }

    /**
     * UC-01.6: Delete Account
     * DELETE /api/auth/account
     * 
     * @param password Current password for confirmation
     * @param user Current authenticated user
     * @return Success message
     */
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestParam String password,
            @com.swd.ticketbook.security.CurrentUser com.swd.ticketbook.entities.User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        authService.deleteAccount(user.getUserId(), password);
        
        return ResponseEntity.ok(
            ApiResponse.success("Account deleted successfully. You can recover it within 30 days by contacting support.")
        );
    }

    /**
     * UC-01.7: Change Password
     * POST /api/auth/change-password
     * 
     * @param request Change password details
     * @param user Current authenticated user
     * @return Success message
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @com.swd.ticketbook.security.CurrentUser com.swd.ticketbook.entities.User user) {
        
        if (user == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }
        
        authService.changePassword(user.getUserId(), request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Password changed successfully! Please login again with your new password.")
        );
    }

    /**
     * Health check endpoint
     * GET /api/auth/health
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("Authentication service is running", "OK")
        );
    }
}

