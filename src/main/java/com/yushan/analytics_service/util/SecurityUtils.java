package com.yushan.analytics_service.util;

import com.yushan.analytics_service.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Security utility class for getting current user information
 */
public class SecurityUtils {

    /**
     * Get current user ID from SecurityContext
     * @return Current user ID as UUID
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return UUID.fromString(userDetails.getUserId());
        }
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Get current user details
     * @return Current user details
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Check if current user is authenticated
     * @return true if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails;
    }
}
