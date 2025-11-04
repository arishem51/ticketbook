package com.swd.ticketbook.services;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * Service for managing verification codes
 * Verification codes expire after 5 minutes
 * Maximum 3 verification attempts allowed
 */
@Service
public class VerificationCodeService {

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    private final SecureRandom random = new SecureRandom();
    
    // In-memory storage for verification codes
    // TODO: Consider using Redis for production
    private final Map<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();

    /**
     * Generate a 6-digit verification code
     */
    public String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Store verification code for email/phone
     * 
     * @param identifier Email or phone number
     * @return Generated verification code
     */
    public String createVerificationCode(String identifier) {
        String code = generateCode();
        VerificationData data = new VerificationData(code, LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCodes.put(identifier, data);
        return code;
    }

    /**
     * Verify code for email/phone
     * 
     * @param identifier Email or phone number
     * @param code Code to verify
     * @return true if code is valid
     */
    public boolean verifyCode(String identifier, String code) {
        VerificationData data = verificationCodes.get(identifier);
        
        if (data == null) {
            return false;
        }

        // Check if expired
        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            verificationCodes.remove(identifier);
            return false;
        }

        // Check attempts
        if (data.attempts >= MAX_ATTEMPTS) {
            verificationCodes.remove(identifier);
            return false;
        }

        data.attempts++;

        // Verify code
        if (data.code.equals(code)) {
            verificationCodes.remove(identifier);
            return true;
        }

        return false;
    }

    /**
     * Check if verification code exists and is valid
     */
    public boolean hasValidCode(String identifier) {
        VerificationData data = verificationCodes.get(identifier);
        if (data == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            verificationCodes.remove(identifier);
            return false;
        }
        
        return true;
    }

    /**
     * Remove verification code
     */
    public void removeCode(String identifier) {
        verificationCodes.remove(identifier);
    }

    /**
     * Clean up expired codes (should be called periodically)
     */
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().expiryTime)
        );
    }

    /**
     * Inner class to store verification data
     */
    private static class VerificationData {
        String code;
        LocalDateTime expiryTime;
        int attempts;

        VerificationData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
            this.attempts = 0;
        }
    }
}

