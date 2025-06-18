package com.example.flutterbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.UserRepository;
import com.example.flutterbackend.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        System.out.println("=== JWT Filter Processing ===");
        System.out.println("Request: " + method + " " + requestURI);
        System.out.println("Authorization Header: " + (authorizationHeader != null ? "Present" : "Not Present"));

        String userEmail = null;
        String jwt = null;
        Long userId = null;

        try {
            // Extract JWT from Authorization header
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                System.out.println("JWT Token extracted, length: " + jwt.length());
                
                // Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    System.err.println("Token is blacklisted - access denied");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Token is blacklisted\"}");
                    return;
                }
                
                try {
                    userEmail = jwtUtil.extractUsername(jwt);
                    userId = jwtUtil.extractUserId(jwt);
                    System.out.println("Extracted - Email: " + userEmail + ", UserId: " + userId);
                } catch (Exception e) {
                    System.err.println("Invalid JWT token: " + e.getMessage());
                }
            }

            // Validate token and set authentication
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("Validating token for user: " + userEmail);
                
                // Validate token
                if (jwtUtil.validateToken(jwt, userEmail)) {
                    System.out.println("Token is valid");
                    
                    // Get user from database
                    User user = userRepository.findByUserEmail(userEmail).orElse(null);
                    
                    if (user != null && !user.getIsDeleted()) {
                        System.out.println("User found and active: " + user.getUserId());
                        
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        // Add userId and token to request attributes for easy access in controllers
                        request.setAttribute("userId", userId);
                        request.setAttribute("userEmail", userEmail);
                        request.setAttribute("jwtToken", jwt); // Add token to request
                        
                        System.out.println("Authentication set successfully for user: " + userId);
                    } else {
                        System.err.println("User not found or deleted for email: " + userEmail);
                    }
                } else {
                    System.err.println("Token validation failed");
                }
            }
        } catch (Exception e) {
            System.err.println("Cannot set user authentication: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== JWT Filter Complete ===");
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip JWT filter for public endpoints - REMOVE /api/users/logout from here
        boolean shouldSkip = path.equals("/api/users/register") || 
                           path.equals("/api/users/login") ||
                           path.equals("/error") ||
                           path.startsWith("/uploads/") ||
                           path.startsWith("/api/debug/");
        
        System.out.println("Should skip JWT filter for " + path + ": " + shouldSkip);
        return shouldSkip;
    }
}
