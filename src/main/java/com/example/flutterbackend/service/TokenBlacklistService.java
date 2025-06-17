package com.example.flutterbackend.service;

import com.example.flutterbackend.model.TokenBlacklist;
import com.example.flutterbackend.repository.TokenBlacklistRepository;
import com.example.flutterbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenBlacklistService {
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // Add token to blacklist
    public void blacklistToken(String token, Long userId) {
        try {
            System.out.println("=== Blacklisting token for user: " + userId + " ===");
            
            // Extract expiry date from token
            Date expiryDate = jwtUtil.extractExpiration(token);
            
            // Create blacklist entry
            TokenBlacklist blacklistEntry = new TokenBlacklist(token, userId, expiryDate);
            
            // Save to database
            tokenBlacklistRepository.save(blacklistEntry);
            
            System.out.println("Token blacklisted successfully");
        } catch (Exception e) {
            System.err.println("Error blacklisting token: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        try {
            return tokenBlacklistRepository.existsByToken(token);
        } catch (Exception e) {
            System.err.println("Error checking blacklisted token: " + e.getMessage());
            return false;
        }
    }
    
    // Clean up expired tokens (run every hour)
    @Scheduled(fixedRate = 3600000/4) // 1 hour
    public void cleanupExpiredTokens() {
        try {
            System.out.println("=== Cleaning up expired blacklisted tokens ===");
            Date now = new Date();
            tokenBlacklistRepository.deleteAllExpired(now);
            System.out.println("Expired tokens cleaned up successfully");
        } catch (Exception e) {
            System.err.println("Error cleaning up expired tokens: " + e.getMessage());
        }
    }
}
