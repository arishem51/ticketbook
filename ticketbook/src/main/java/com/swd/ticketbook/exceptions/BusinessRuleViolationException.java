package com.swd.ticketbook.exceptions;

/**
 * Exception thrown when a business rule is violated
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    private String businessRuleCode;

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, String businessRuleCode) {
        super(message);
        this.businessRuleCode = businessRuleCode;
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getBusinessRuleCode() {
        return businessRuleCode;
    }
}

