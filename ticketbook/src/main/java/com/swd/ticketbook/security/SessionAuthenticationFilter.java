package com.swd.ticketbook.security;

import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.services.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Authentication filter for session-based authentication
 * Validates session token from Authorization header
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            String sessionToken = extractSessionToken(request);
            
            if (sessionToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOpt = sessionService.validateSession(sessionToken);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Check if user account is deleted
                    if (user.getIsDeleted()) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Store user in request attribute for easy access in controllers
                    request.setAttribute("currentUser", user);
                }
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract session token from Authorization header
     * Format: "Bearer {token}" or just "{token}"
     */
    private String extractSessionToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && !bearerToken.isBlank()) {
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return bearerToken;
        }
        
        return null;
    }
}

