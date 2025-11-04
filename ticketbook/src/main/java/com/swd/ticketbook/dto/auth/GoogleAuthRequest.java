package com.swd.ticketbook.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Google OAuth authentication (UC-01.1, UC-01.2)
 */
public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    // Constructors
    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String idToken) {
        this.idToken = idToken;
    }

    // Getters and Setters
    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}

