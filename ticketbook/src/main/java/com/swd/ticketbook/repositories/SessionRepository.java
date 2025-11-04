package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.Session;
import com.swd.ticketbook.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Session entity
 * Supports session-based authentication
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * Find session by token
     * Used for authentication validation
     */
    Optional<Session> findBySessionToken(String sessionToken);

    /**
     * Find active sessions for a user
     * Used for session management
     */
    List<Session> findByUserAndIsActiveTrue(User user);

    /**
     * Find all active sessions for a user
     */
    @Query("SELECT s FROM Session s WHERE s.user = :user AND s.isActive = true AND s.expiresAt > :now")
    List<Session> findActiveSessionsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Invalidate all sessions for a user
     * Used during password change (UC-01.7) or account deletion
     */
    @Modifying
    @Query("UPDATE Session s SET s.isActive = false WHERE s.user = :user")
    void invalidateAllUserSessions(@Param("user") User user);

    /**
     * Delete expired sessions (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now OR s.isActive = false")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Count active sessions for a user
     */
    long countByUserAndIsActiveTrueAndExpiresAtAfter(User user, LocalDateTime now);

    /**
     * Find session by token and check if valid
     */
    @Query("SELECT s FROM Session s WHERE s.sessionToken = :token AND s.isActive = true AND s.expiresAt > :now")
    Optional<Session> findValidSession(@Param("token") String token, @Param("now") LocalDateTime now);
}

