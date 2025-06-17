package com.example.flutterbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import com.example.flutterbackend.model.User;
import com.example.flutterbackend.dto.UserLoginDTO;
import com.example.flutterbackend.dto.UserRegistrationDTO;
import com.example.flutterbackend.dto.UpdateUserProfileDTO;
import com.example.flutterbackend.dto.UserDTO;
import com.example.flutterbackend.repository.UserRepository;
import com.example.flutterbackend.security.JwtUtil;
import com.example.flutterbackend.service.FileStorageService;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FileStorageService fileStorageService;

    // Konversi User entity ke UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserProfile(user.getUserProfile());
        dto.setIsDeleted(user.getIsDeleted());
        
        // Tambahkan profileUrl untuk akses file
        if (user.getUserProfile() != null) {
            dto.setProfileUrl(fileStorageService.getFileUrl(user.getUserProfile()));
        }
        
        return dto;
    }

    // Get User entity by email (untuk session creation)
    public User getUserEntityByEmail(String email) {
        return userRepository.findByUserEmail(email).orElse(null);
    }

    // Registrasi user baru dengan password hashing
    public UserDTO registrationUser(UserRegistrationDTO request) {
        try {
            System.out.println("=== UserService.registrationUser() called ===");
            
            // Validasi input
            if (request.getUserEmail() == null || request.getUserEmail().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email wajib diisi!");
            }

            if (request.getUserPassword() == null || request.getUserPassword().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password wajib diisi!");
            }

            // Cek apakah email sudah terdaftar
            Optional<User> existingUser = userRepository.findByUserEmail(request.getUserEmail());
            if (existingUser.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah digunakan");
            }

            // Buat user baru dengan password yang di-hash
            User user = new User();
            user.setUserName(request.getUserName());
            user.setUserEmail(request.getUserEmail());
            user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
            user.setUserProfile(request.getUserProfile() != null ? request.getUserProfile() : "default.jpg");
            user.setIsDeleted(false);

            // Simpan ke database
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error in registrationUser: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    // Login user dengan JWT token generation
    public LoginResponse login(UserLoginDTO request) {
        try {
            System.out.println("=== UserService.login() called ===");
            
            // Validasi input
            if (request.getUserEmail() == null || request.getUserEmail().isEmpty() || 
                request.getUserPassword() == null || request.getUserPassword().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email dan password wajib diisi");
            }

            // Cari user berdasarkan email
            User user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah"));

            // Validasi password dengan BCrypt
            if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUserEmail());

            // Return response dengan token
            return new LoginResponse(convertToDTO(user), token);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error in login: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed: " + e.getMessage());
        }
    }

    // Update profile user (terutama foto profil)
    public UserDTO updateProfile(Long userId, UpdateUserProfileDTO request) {
        try {
            System.out.println("=== UserService.updateProfile() called for user: " + userId + " ===");
            
            // Cari user berdasarkan ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

            // Update profile
            if (request.getUserName() != null) {
                user.setUserName(request.getUserName());
            }
            if (request.getUserProfile() != null) {
                user.setUserProfile(request.getUserProfile());
            }

            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update profile failed: " + e.getMessage());
        }
    }

    // Update password user
    public UserDTO updatePassword(Long userId, String currentPassword, String newPassword) {
        try {
            System.out.println("=== UserService.updatePassword() called for user: " + userId + " ===");
            
            if (newPassword == null || newPassword.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password baru wajib diisi");
            }
            
            // Cari user berdasarkan ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

            // Validasi password lama
            if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password lama tidak sesuai");
            }

            // Update password
            user.setUserPassword(passwordEncoder.encode(newPassword));
            User updatedUser = userRepository.save(user);
            
            return convertToDTO(updatedUser);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update password failed: " + e.getMessage());
        }
    }

    // Ambil user berdasarkan ID
    public UserDTO getUserById(Long userId) {
        try {
            System.out.println("=== UserService.getUserById() called for ID: " + userId + " ===");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            return convertToDTO(user);
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Get user failed: " + e.getMessage());
        }
    }

    // Soft delete user
    public UserDTO deleteUser(Long userId) {
        try {
            System.out.println("=== UserService.deleteUser() called for ID: " + userId + " ===");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            
            user.setIsDeleted(true);
            User savedUser = userRepository.save(user);
            return convertToDTO(savedUser);
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete user failed: " + e.getMessage());
        }
    }

    // Update profile user dengan foto
    public UserDTO updateProfileWithPhoto(Long userId, String userName, MultipartFile profilePhoto) {
        try {
            System.out.println("=== UserService.updateProfileWithPhoto() called for user: " + userId + " ===");
            
            // Cari user berdasarkan ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

            // Update nama jika ada
            if (userName != null && !userName.trim().isEmpty()) {
                user.setUserName(userName);
            }

            // Update foto profil jika ada
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                // Hapus foto lama jika bukan default
                String oldProfilePhoto = user.getUserProfile();
                if (oldProfilePhoto != null && !oldProfilePhoto.equals("default.jpg")) {
                    fileStorageService.deleteFile(oldProfilePhoto);
                }
                
                // Simpan foto baru
                String newFileName = fileStorageService.storeFile(profilePhoto);
                user.setUserProfile(newFileName);
            }

            User updatedUser = userRepository.save(user);
            UserDTO userDTO = convertToDTO(updatedUser);
            
            // Tambahkan URL foto profil
            if (updatedUser.getUserProfile() != null) {
                userDTO.setProfileUrl(fileStorageService.getFileUrl(updatedUser.getUserProfile()));
            }
            
            return userDTO;
        } catch (Exception e) {
            System.err.println("Error updating profile with photo: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Update profile failed: " + e.getMessage());
        }
    }

    // Inner class untuk login response
    public static class LoginResponse {
        private UserDTO user;
        private String token;

        public LoginResponse(UserDTO user, String token) {
            this.user = user;
            this.token = token;
        }

        // Getters and setters
        public UserDTO getUser() { return user; }
        public void setUser(UserDTO user) { this.user = user; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
