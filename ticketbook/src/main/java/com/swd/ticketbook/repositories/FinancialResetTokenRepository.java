package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.FinancialResetToken;
import com.swd.ticketbook.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for FinancialResetToken entity
 * Supports UC-01.3: Reset Password functionality
 */
@Repository
public interface FinancialResetTokenRepository extends JpaRepository<FinancialResetToken, Long> {

    /**
     * Find token by token value
     * Used during password reset
     */
    Optional<FinancialResetToken> findByTokenValue(String tokenValue);

    /**
     * Find valid (unused and not expired) token
     */
    @Query("SELECT t FROM FinancialResetToken t WHERE t.tokenValue = :token AND t.isUsed = false AND t.expiryTime > :now")
    Optional<FinancialResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Find active tokens for a user
     */
    @Query("SELECT t FROM FinancialResetToken t WHERE t.user = :user AND t.isUsed = false AND t.expiryTime > :now")
    Optional<FinancialResetToken> findActiveTokenByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Invalidate all tokens for a user (when one is used or password is changed)
     */
    @Modifying
    @Query("UPDATE FinancialResetToken t SET t.isUsed = true WHERE t.user = :user AND t.isUsed = false")
    void invalidateAllUserTokens(@Param("user") User user);

    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM FinancialResetToken t WHERE t.expiryTime < :now OR t.isUsed = true")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count active tokens for a user
     */
    long countByUserAndIsUsedFalseAndExpiryTimeAfter(User user, LocalDateTime now);
}

