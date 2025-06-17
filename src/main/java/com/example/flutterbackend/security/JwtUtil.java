package com.example.flutterbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;

    // Generate signing key from secret
    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            System.err.println("Error generating signing key: " + e.getMessage());
            throw new RuntimeException("Failed to generate JWT signing key", e);
        }
    }

    // Generate token dengan user ID dan email
    public String generateToken(Long userId, String userEmail) {
        try {
            System.out.println("=== JwtUtil.generateToken() called ===");
            System.out.println("Generating token for user ID: " + userId + ", email: " + userEmail);
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("userEmail", userEmail);
            
            String token = createToken(claims, userEmail);
            System.out.println("Token generated successfully, length: " + token.length());
            return token;
        } catch (Exception e) {
            System.err.println("Error generating JWT token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JWT token generation failed", e);
        }
    }

    // Create token
    private String createToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            System.err.println("Error creating JWT token: " + e.getMessage());
            throw new RuntimeException("Failed to create JWT token", e);
        }
    }

    // Extract username (email) from token
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            System.err.println("Error extracting username from token: " + e.getMessage());
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }

    // Extract user ID from token
    public Long extractUserId(String token) {
        try {
            return extractClaim(token, claims -> {
                Object userId = claims.get("userId");
                if (userId instanceof Integer) {
                    return ((Integer) userId).longValue();
                } else if (userId instanceof Long) {
                    return (Long) userId;
                }
                return null;
            });
        } catch (Exception e) {
            System.err.println("Error extracting user ID from token: " + e.getMessage());
            throw new RuntimeException("Failed to extract user ID from token", e);
        }
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            System.err.println("Error extracting expiration from token: " + e.getMessage());
            throw new RuntimeException("Failed to extract expiration from token", e);
        }
    }

    // Extract claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token has expired: " + e.getMessage());
            throw new RuntimeException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            System.err.println("JWT token is malformed: " + e.getMessage());
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SecurityException e) {
            System.err.println("JWT signature validation failed: " + e.getMessage());
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            System.err.println("JWT token compact of handler are invalid: " + e.getMessage());
            throw new RuntimeException("JWT token compact of handler are invalid", e);
        } catch (Exception e) {
            System.err.println("Unexpected error parsing JWT token: " + e.getMessage());
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true; // Consider expired if any error occurs
        }
    }

    // Validate token with user email
    public Boolean validateToken(String token, String userEmail) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userEmail) && !isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    // Validate token without user details
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    // Get token from Authorization header
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
