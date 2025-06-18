package com.example.flutterbackend.service;

import com.example.flutterbackend.dto.UpdateUserProfileDTO;
import com.example.flutterbackend.dto.UserDTO;
import com.example.flutterbackend.dto.UserLoginDTO;
import com.example.flutterbackend.dto.UserRegistrationDTO;
import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.UserRepository;
import com.example.flutterbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    /**
     * Method internal untuk mengubah User Entity menjadi UserDTO.
     * DTO adalah objek yang dikirim ke frontend.
     * Method ini memastikan 'profileUrl' selalu memiliki nilai (URL kustom atau URL default).
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserProfile(user.getUserProfile()); // Ini adalah nama file internal.
        dto.setIsDeleted(user.getIsDeleted());

        // Ini membuat URL lengkap yang bisa diakses dari Flutter.
        dto.setProfileUrl(fileStorageService.getFileUrl(user.getUserProfile()));

        return dto;
    }

    /**
     * Registrasi pengguna baru.
     * Logikanya sama persis dengan kode Anda.
     */
    public UserDTO registrationUser(UserRegistrationDTO request) {
        if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah digunakan");
        }
        User user = new User();
        user.setUserName(request.getUserName());
        user.setUserEmail(request.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setUserProfile("default.jpg"); // Pengguna baru selalu mendapat foto default.
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Login pengguna.
     * Logikanya sama persis dengan kode Anda.
     */
    public LoginResponse login(UserLoginDTO request) {
        User user = userRepository.findByUserEmail(request.getUserEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah"));

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserEmail());
        return new LoginResponse(convertToDTO(user), token);
    }

    /**
     * Mendapatkan data user berdasarkan ID.
     * Logikanya sama persis dengan kode Anda.
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        return convertToDTO(user);
    }

    /**
     * Memperbarui profil (hanya nama).
     * Logikanya sama persis dengan kode Anda.
     */
    public UserDTO updateProfile(Long userId, UpdateUserProfileDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

        if (request.getUserName() != null && !request.getUserName().trim().isEmpty()) {
            user.setUserName(request.getUserName());
        }
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    /**
     * Memperbarui profil dengan foto baru.
     * Logikanya sama, hanya sedikit lebih ringkas.
     */
    public UserDTO updateProfileWithPhoto(Long userId, String userName, MultipartFile profilePhoto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));

        // Update nama jika dikirim bersamaan dengan foto
        if (userName != null && !userName.trim().isEmpty()) {
            user.setUserName(userName);
        }

        // Proses foto jika ada
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String oldProfilePhoto = user.getUserProfile();
            // Hapus foto lama jika bukan default
            if (oldProfilePhoto != null && !oldProfilePhoto.equals("default.jpg")) {
                fileStorageService.deleteFile(oldProfilePhoto);
            }
            // Simpan foto baru
            String newFileName = fileStorageService.storeFile(profilePhoto);
            user.setUserProfile(newFileName);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Menghapus foto profil kustom.
     * Ini adalah method yang kita perbaiki.
     */
    public UserDTO deleteProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        String oldFilename = user.getUserProfile();

        // Hapus file fisik jika ada dan bukan default
        if (oldFilename != null && !oldFilename.isEmpty() && !oldFilename.equals("default.jpg")) {
            fileStorageService.deleteFile(oldFilename);
        }

        // Atur ulang kolom di database menjadi null
        user.setUserProfile(null);
        User updatedUser = userRepository.save(user);

        // Kembalikan DTO yang sudah diperbarui. convertToDTO akan otomatis memberi URL default.
        return convertToDTO(updatedUser);
    }
    
    /**
     * Memperbarui password.
     * Logikanya sama persis dengan kode Anda.
     */
    public UserDTO updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan"));
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password lama tidak sesuai");
        }
        user.setUserPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Menghapus akun (Soft Delete).
     * Logikanya sama persis dengan kode Anda.
     */
    public UserDTO deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        user.setIsDeleted(true);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Inner class untuk struktur response login.
     */
    public static class LoginResponse {
        private final UserDTO user;
        private final String token;

        public LoginResponse(UserDTO user, String token) {
            this.user = user;
            this.token = token;
        }

        // Getters
        public UserDTO getUser() { return user; }
        public String getToken() { return token; }
    }
}