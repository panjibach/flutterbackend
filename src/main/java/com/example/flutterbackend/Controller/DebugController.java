package com.example.flutterbackend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.TokenBlacklistRepository;
import com.example.flutterbackend.repository.UserRepository;
import com.example.flutterbackend.security.JwtUtil;
import com.example.flutterbackend.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // Test endpoint untuk cek koneksi
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }

    // Test password encoding
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Map<String, Object> response = new HashMap<>();
            
            // Find user
            Optional<User> userOpt = userRepository.findByUserEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean matches = passwordEncoder.matches(password, user.getUserPassword());
                
                response.put("userFound", true);
                response.put("passwordMatches", matches);
                response.put("storedPasswordHash", user.getUserPassword());
                response.put("inputPassword", password);
                response.put("encodedInputPassword", passwordEncoder.encode(password));
            } else {
                response.put("userFound", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Test JWT generation
    @PostMapping("/test-jwt")
    public ResponseEntity<?> testJwt(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            Map<String, Object> response = new HashMap<>();
            
            // Find user
            Optional<User> userOpt = userRepository.findByUserEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Generate JWT
                String token = jwtUtil.generateToken(user.getUserId(), user.getUserEmail());
                
                // Validate JWT
                boolean isValid = jwtUtil.validateToken(token);
                String extractedEmail = jwtUtil.extractUsername(token);
                Long extractedUserId = jwtUtil.extractUserId(token);
                
                response.put("userFound", true);
                response.put("tokenGenerated", token);
                response.put("tokenValid", isValid);
                response.put("extractedEmail", extractedEmail);
                response.put("extractedUserId", extractedUserId);
            } else {
                response.put("userFound", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // List all users
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        try {
            var users = userRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("count", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Simple login test
    @PostMapping("/simple-login")
    public ResponseEntity<?> simpleLogin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("=== DEBUG Simple Login ===");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);
            
            // Find user
            Optional<User> userOpt = userRepository.findByUserEmail(email);
            if (!userOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(404).body(error);
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getUserId());
            
            // Check password
            boolean passwordMatches = passwordEncoder.matches(password, user.getUserPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (!passwordMatches) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid password");
                return ResponseEntity.status(401).body(error);
            }
            
            // Generate token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUserEmail());
            System.out.println("Token generated: " + token.substring(0, 20) + "...");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("userEmail", user.getUserEmail());
            response.put("userName", user.getUserName());
            response.put("token", token);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in simple login: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // View blacklisted tokens
    @GetMapping("/blacklisted-tokens")
    public ResponseEntity<?> getBlacklistedTokens() {
        try {
            var tokens = tokenBlacklistRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("tokens", tokens);
            response.put("count", tokens.size());
            response.put("currentTime", new Date());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Check if token is blacklisted
    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(@RequestParam String token) {
        try {
            boolean isBlacklisted = tokenBlacklistRepository.existsByToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("isBlacklisted", isBlacklisted);
            
            if (isBlacklisted) {
                var blacklistEntry = tokenBlacklistRepository.findByToken(token);
                response.put("blacklistDetails", blacklistEntry);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Test current token status
    @GetMapping("/token-status")
    public ResponseEntity<?> getTokenStatus(HttpServletRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Get token from header
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // Check if blacklisted
                boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
                
                // Get token details
                try {
                    String email = jwtUtil.extractUsername(token);
                    Long userId = jwtUtil.extractUserId(token);
                    Date expiry = jwtUtil.extractExpiration(token);
                    boolean isValid = jwtUtil.validateToken(token);
                    
                    response.put("tokenPresent", true);
                    response.put("isBlacklisted", isBlacklisted);
                    response.put("isValid", isValid);
                    response.put("email", email);
                    response.put("userId", userId);
                    response.put("expiry", expiry);
                    response.put("token", token.substring(0, 20) + "...");
                } catch (Exception e) {
                    response.put("tokenPresent", true);
                    response.put("tokenError", e.getMessage());
                }
            } else {
                response.put("tokenPresent", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
