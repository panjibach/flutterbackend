package com.example.flutterbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Define constants for claim keys to avoid magic strings
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_EMAIL = "userEmail";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:300000}") // 24 hours in milliseconds
    private Long expiration;

    private SecretKey key;

    /**
     * Initializes the secret key after the bean has been constructed.
     * This is more efficient than creating the key on every call.
     */
    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            logger.error("JWT Secret is not configured or is too short. It must be at least 256 bits (32 characters).");
            throw new IllegalArgumentException("JWT Secret is not configured or is too short. Please set 'jwt.secret' property.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT Secret Key initialized successfully.");
    }

    /**
     * Generates a JWT for a given user ID and email.
     *
     * @param userId    The ID of the user.
     * @param userEmail The email of the user (used as the subject).
     * @return A signed JWT string.
     */
    public String generateToken(Long userId, String userEmail) {
        logger.debug("Generating JWT for user ID: {}", userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USER_EMAIL, userEmail); // Optional, subject is usually enough

        return createToken(claims, userEmail);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512) // Explicitly set a strong algorithm
                .compact();
    }

    /**
     * Extracts the username (email) from the token's subject.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from the token's claims.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_USER_ID, Long.class));
    }

    /**
     * Extracts the expiration date from the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * A generic function to extract a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token compact of handler is invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates the token against the user's email and checks for expiration.
     *
     * @param token     The JWT token.
     * @param userEmail The email to validate against the token's subject.
     * @return True if the token is valid for the given user and not expired.
     */
    public Boolean validateToken(String token, String userEmail) {
        try {
            final String usernameInToken = extractUsername(token);
            // No need to check for expiration separately, as extractAllClaims would have thrown ExpiredJwtException
            return usernameInToken.equals(userEmail);
        } catch (JwtException e) {
            // Any exception during claim extraction means the token is invalid for our purpose.
            logger.debug("Token validation failed for user {}: {}", userEmail, e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts the token from the "Authorization" header.
     *
     * @param authHeader The full Authorization header value (e.g., "Bearer ey...").
     * @return The token string or null if the header is invalid.
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}