package com.example.flutterbackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.flutterbackend.model.User;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthenticationHelper {

    // Get current authenticated user
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current authentication: " + (authentication != null ? authentication.getName() : "null"));
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    // Get current user ID
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        Long userId = user != null ? user.getUserId() : null;
        System.out.println("Current user ID: " + userId);
        return userId;
    }

    // Get user ID from request attributes (set by JWT filter)
    public Long getUserIdFromRequest(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        System.out.println("User ID from request attribute: " + userId);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    // Get user email from request attributes
    public String getUserEmailFromRequest(HttpServletRequest request) {
        Object userEmail = request.getAttribute("userEmail");
        System.out.println("User email from request attribute: " + userEmail);
        return userEmail instanceof String ? (String) userEmail : null;
    }

    // Check if current user owns the resource
    public boolean isResourceOwner(Long resourceUserId) {
        Long currentUserId = getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(resourceUserId);
        System.out.println("Resource owner check - Current: " + currentUserId + ", Resource: " + resourceUserId + ", IsOwner: " + isOwner);
        return isOwner;
    }

    // Validate that path userId matches authenticated user
    public boolean validateUserAccess(Long pathUserId, HttpServletRequest request) {
        Long authenticatedUserId = getUserIdFromRequest(request);
        boolean hasAccess = authenticatedUserId != null && authenticatedUserId.equals(pathUserId);
        System.out.println("User access validation - Authenticated: " + authenticatedUserId + ", Path: " + pathUserId + ", HasAccess: " + hasAccess);
        return hasAccess;
    }

    // Check if user is authenticated
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String && 
                 "anonymousUser".equals(authentication.getPrincipal()));
        System.out.println("Is authenticated: " + authenticated);
        return authenticated;
    }
}
