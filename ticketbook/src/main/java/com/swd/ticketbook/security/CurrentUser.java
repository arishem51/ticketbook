package com.swd.ticketbook.security;

import java.lang.annotation.*;

/**
 * Annotation to inject current authenticated user into controller methods
 * Used with @CurrentUser User user parameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}

