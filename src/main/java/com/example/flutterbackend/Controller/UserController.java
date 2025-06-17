package com.example.flutterbackend.Controller;

import com.example.flutterbackend.dto.UserRegistrationDTO;
import com.example.flutterbackend.dto.UserLoginDTO;
import com.example.flutterbackend.dto.UpdateUserProfileDTO;
import com.example.flutterbackend.dto.UserDTO;
import com.example.flutterbackend.service.UserService;
import com.example.flutterbackend.security.AuthenticationHelper;
import com.example.flutterbackend.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationHelper authHelper;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // ========== FITUR DASAR USER ==========
    
    // 1. REGISTER: Registrasi user baru
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegistrationDTO request) {
        try {
            System.out.println("=== POST /api/users/register called ===");
            UserDTO registeredUser = userService.registrationUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            throw e;
        }
    }

    // 2. LOGIN: Login user - SIMPLIFIED WITHOUT SESSION
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== POST /api/users/login called ===");
            UserService.LoginResponse loginResponse = userService.login(request);
            
            // NO SESSION CREATION - ONLY JWT
            System.out.println("Login successful, returning JWT token only");
            
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            System.err.println("Error in login: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    // 3. LOGOUT: Logout user - WITH TOKEN BLACKLISTING
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        try {
            System.out.println("=== POST /api/users/logout called ===");
            
            // Get userId from request attribute (set by JWT filter)
            Long userId = authHelper.getUserIdFromRequest(request);
            System.out.println("User ID from request: " + userId);
            
            // Get token from request attribute (set by JWT filter)
            String token = (String) request.getAttribute("jwtToken");
            System.out.println("Token from request: " + (token != null ? "Present" : "Not Present"));
            
            // Fallback: get token from Authorization header if not in attribute
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    System.out.println("Token extracted from header as fallback");
                }
            }
            
            // Add token to blacklist if we have both userId and token
            if (userId != null && token != null) {
                tokenBlacklistService.blacklistToken(token, userId);
                System.out.println("Token blacklisted successfully for user: " + userId);
            } else {
                System.err.println("Cannot blacklist token - userId: " + userId + ", token: " + (token != null ? "present" : "null"));
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout berhasil");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout gagal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 4. GET PROFILE: Ambil data user berdasarkan ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            System.out.println("=== GET /api/users/" + userId + " called ===");
            
            // Validasi akses
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke data user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            throw e;
        }
    }

    // 5. UPDATE PROFILE: Update profile user (terutama foto)
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @PathVariable Long userId, 
            @Valid @RequestBody UpdateUserProfileDTO request,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("=== PUT /api/users/" + userId + "/profile called ===");
            
            // Validasi akses
            if (!authHelper.validateUserAccess(userId, httpRequest)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk mengupdate profile user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            UserDTO updatedUser = userService.updateProfile(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            throw e;
        }
    }

    // 6. UPDATE PASSWORD: Update password user
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordData,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== PUT /api/users/" + userId + "/password called ===");
            
            // Validasi akses
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk mengupdate password user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password lama dan baru wajib diisi");
                return ResponseEntity.badRequest().body(error);
            }
            
            UserDTO updatedUser = userService.updatePassword(userId, currentPassword, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password berhasil diupdate");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 7. DELETE USER: Hapus akun user (soft delete)
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== DELETE /api/users/" + userId + " called ===");
            
            // Validasi akses
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk menghapus user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            UserDTO deletedUser = userService.deleteUser(userId);
            
            // Blacklist current token
            String token = (String) request.getAttribute("jwtToken");
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            if (token != null) {
                tokenBlacklistService.blacklistToken(token, userId);
                System.out.println("Token blacklisted after user deletion");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Akun berhasil dihapus");
            response.put("user", deletedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Upload foto profil
    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long userId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) MultipartFile profilePhoto,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== POST /api/users/" + userId + "/profile-photo called ===");
            
            // Validasi akses
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk mengupdate profile user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validasi file
            if (profilePhoto == null || profilePhoto.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File foto profil wajib diupload");
                return ResponseEntity.badRequest().body(error);
            }
            
            UserDTO updatedUser = userService.updateProfileWithPhoto(userId, userName, profilePhoto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Foto profil berhasil diupdate");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error uploading profile photo: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========== ERROR HANDLING ==========
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        System.err.println("Exception caught: " + ex.getMessage());
        ex.printStackTrace();
        
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
