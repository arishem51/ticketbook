package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for User entity
 * Supports UC-01: Authentication operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by contact (email or phone)
     * Used for login and registration validation (FR9)
     */
    Optional<User> findByContact(String contact);

    /**
     * Find user by contact (case-insensitive for emails)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.contact) = LOWER(:contact)")
    Optional<User> findByContactIgnoreCase(@Param("contact") String contact);

    /**
     * Find user by OAuth provider and provider ID
     * Used for Google OAuth authentication
     */
    Optional<User> findByOauthProviderAndOauthProviderId(String oauthProvider, String oauthProviderId);

    /**
     * Check if contact exists (FR9)
     */
    boolean existsByContact(String contact);

    /**
     * Check if contact exists (case-insensitive)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.contact) = LOWER(:contact)")
    boolean existsByContactIgnoreCase(@Param("contact") String contact);

    /**
     * Find active (non-deleted) users by role
     * Used for admin user management
     */
    List<User> findByRoleAndIsDeletedFalse(UserRole role);

    /**
     * Find all non-deleted users
     * Used for admin user management (UC-04.1)
     */
    List<User> findByIsDeletedFalse();

    /**
     * Count users by role
     */
    long countByRoleAndIsDeletedFalse(UserRole role);

    /**
     * Find users registered after a specific date
     */
    @Query("SELECT u FROM User u WHERE u.registrationDate >= :fromDate AND u.isDeleted = false")
    List<User> findRecentUsers(@Param("fromDate") java.time.LocalDateTime fromDate);
}
