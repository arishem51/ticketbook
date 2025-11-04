package com.swd.ticketbook.dto.auth;

/**
 * DTO for authentication response (login/register)
 * Contains session token and user information
 */
public class AuthResponse {

    private String sessionToken;
    private UserResponse user;
    private String message;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String sessionToken, UserResponse user, String message) {
        this.sessionToken = sessionToken;
        this.user = user;
        this.message = message;
    }

    // Getters and Setters
    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

