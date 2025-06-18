package com.example.flutterbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // === PERUBAHAN DI SINI: Inject base URL dari application.properties ===
    @Value("${app.base-url}")
    private String baseUrl;

    private Path getUploadPath() {
        // ... (method ini tidak berubah)
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            return uploadPath;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Tidak dapat membuat direktori untuk menyimpan file", e);
        }
    }

    public String storeFile(MultipartFile file) {
        // ... (method ini tidak berubah)
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tidak boleh kosong");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Hanya file gambar yang diperbolehkan (JPEG, PNG, GIF, dll)");
            }
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = getUploadPath().resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File berhasil disimpan: " + filename);
            return filename;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String filename) {
        // ... (method ini tidak berubah)
        if (filename == null || filename.isEmpty() || filename.equals("default.jpg")) {
            return;
        }
        try {
            Path filePath = getUploadPath().resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("File berhasil dihapus: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Gagal menghapus file: " + e.getMessage());
        }
    }

    // === PERUBAHAN DI SINI: Membuat URL Absolut/Lengkap ===
    public String getFileUrl(String filename) {
        if (filename == null || filename.isEmpty() || filename.equals("default.jpg")) {
            // Anda bisa memilih untuk men-host gambar default Anda sendiri
            // atau menggunakan placeholder dari internet.
            // Untuk saat ini, kita arahkan ke file default yang juga disajikan oleh backend.
            return baseUrl + "/uploads/default.jpg";
        }
        return baseUrl + "/uploads/" + filename;
    }
}