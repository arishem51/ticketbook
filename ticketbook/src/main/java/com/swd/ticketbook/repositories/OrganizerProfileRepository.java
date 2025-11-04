package com.swd.ticketbook.repositories;

import com.swd.ticketbook.entities.OrganizerProfile;
import com.swd.ticketbook.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OrganizerProfile entity
 * Supports UC-03.1: KYC verification for organizers (FR26)
 */
@Repository
public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, Long> {

    /**
     * Find organizer profile by user
     */
    Optional<OrganizerProfile> findByUser(User user);

    /**
     * Find profiles by KYC status
     * Used for admin review of pending verifications
     */
    List<OrganizerProfile> findByKycStatus(String kycStatus);

    /**
     * Find pending KYC verifications
     * Used for admin dashboard
     */
    @Query("SELECT op FROM OrganizerProfile op WHERE op.kycStatus = 'PENDING_VERIFICATION' ORDER BY op.kycSubmittedAt ASC")
    List<OrganizerProfile> findPendingKycVerifications();

    /**
     * Count profiles by KYC status
     */
    long countByKycStatus(String kycStatus);

    /**
     * Check if user has organizer profile
     */
    boolean existsByUser(User user);
}

