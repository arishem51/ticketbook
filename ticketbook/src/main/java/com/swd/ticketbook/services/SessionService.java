package com.swd.ticketbook.services;

import com.swd.ticketbook.entities.Session;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.repositories.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing user sessions
 * Implements session-based authentication
 */
@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    /**
     * Create a new session for user
     * Session expires after 24 hours
     * 
     * @param user User to create session for
     * @param request HTTP request for IP and user agent
     * @return Session token
     */
    @Transactional
    public String createSession(User user, HttpServletRequest request) {
        String sessionToken = generateSessionToken();
        
        Session session = new Session(sessionToken, user);
        session.setIpAddress(getClientIP(request));
        session.setUserAgent(getUserAgent(request));
        
        sessionRepository.save(session);
        
        return sessionToken;
    }

    /**
     * Validate session token and return user
     * Also updates last activity time
     * 
     * @param sessionToken Session token to validate
     * @return User if session is valid
     */
    @Transactional
    public Optional<User> validateSession(String sessionToken) {
        Optional<Session> sessionOpt = sessionRepository.findValidSession(
            sessionToken, 
            LocalDateTime.now()
        );
        
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            session.updateActivity();
            sessionRepository.save(session);
            return Optional.of(session.getUser());
        }
        
        return Optional.empty();
    }

    /**
     * Get session by token
     * 
     * @param sessionToken Session token
     * @return Session if found
     */
    public Optional<Session> getSession(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken);
    }

    /**
     * Invalidate a specific session (logout)
     * 
     * @param sessionToken Session token to invalidate
     */
    @Transactional
    public void invalidateSession(String sessionToken) {
        Optional<Session> sessionOpt = sessionRepository.findBySessionToken(sessionToken);
        sessionOpt.ifPresent(session -> {
            session.invalidate();
            sessionRepository.save(session);
        });
    }

    /**
     * Invalidate all sessions for a user
     * Used during password change (UC-01.7)
     * 
     * @param user User whose sessions to invalidate
     */
    @Transactional
    public void invalidateAllUserSessions(User user) {
        sessionRepository.invalidateAllUserSessions(user);
    }

    /**
     * Get all active sessions for a user
     * 
     * @param user User to get sessions for
     * @return List of active sessions
     */
    public List<Session> getActiveUserSessions(User user) {
        return sessionRepository.findActiveSessionsByUser(user, LocalDateTime.now());
    }

    /**
     * Clean up expired sessions
     * Should be called periodically by a scheduled task
     */
    @Transactional
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }

    /**
     * Generate a unique session token
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * Extract user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : null;
    }
}

