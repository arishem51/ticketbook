package com.swd.ticketbook.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for verification code validation
 * Used during registration and profile updates
 */
public class VerificationRequest {

    private String email;
    private String phone;

    @NotBlank(message = "Verification code is required")
    private String code;

    // Constructors
    public VerificationRequest() {
    }

    public VerificationRequest(String emailOrPhone, String code) {
        if (emailOrPhone.contains("@")) {
            this.email = emailOrPhone;
        } else {
            this.phone = emailOrPhone;
        }
        this.code = code;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

