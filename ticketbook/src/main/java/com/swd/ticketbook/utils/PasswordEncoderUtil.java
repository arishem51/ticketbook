package com.swd.ticketbook.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password encoding
 * Business Rule FR1: Password Encoding with MD5 hashing
 * 
 * NOTE: MD5 is deprecated and not secure for production use.
 * Consider using BCrypt or Argon2 for better security.
 */
public class PasswordEncoderUtil {

    /**
     * Encode password using MD5 hash (FR1)
     * 
     * @param password Plain text password
     * @return MD5 hashed password
     */
    public static String encode(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            
            // Pad with leading zeros
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }

    /**
     * Verify password against MD5 hash
     * 
     * @param plainPassword Plain text password to verify
     * @param hashedPassword MD5 hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        String hashedInput = encode(plainPassword);
        return hashedInput.equals(hashedPassword);
    }

    /**
     * Validate password strength (FR10)
     * Password must be at least 8 characters with letters and numbers
     * 
     * @param password Password to validate
     * @return true if password meets requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
}

