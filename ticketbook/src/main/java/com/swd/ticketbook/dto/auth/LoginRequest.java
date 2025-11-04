package com.swd.ticketbook.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login (UC-01.2)
 * Supports login with email/phone and password
 */
public class LoginRequest {

    private String email;
    
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequest() {
    }

    public LoginRequest(String emailOrPhone, String password) {
        if (emailOrPhone.contains("@")) {
            this.email = emailOrPhone;
        } else {
            this.phone = emailOrPhone;
        }
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Validation
    public boolean hasEmailOrPhone() {
        return (email != null && !email.isBlank()) || (phone != null && !phone.isBlank());
    }

    public String getIdentifier() {
        return email != null && !email.isBlank() ? email : phone;
    }
}

