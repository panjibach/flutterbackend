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

    // Inisialisasi direktori upload
    private Path getUploadPath() {
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

    // Simpan file dan return path-nya
    public String storeFile(MultipartFile file) {
        try {
            // Validasi file
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tidak boleh kosong");
            }

            // Validasi tipe file (hanya gambar)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Hanya file gambar yang diperbolehkan (JPEG, PNG, GIF, dll)");
            }

            // Buat nama file unik untuk menghindari konflik
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + fileExtension;

            // Simpan file
            Path targetLocation = getUploadPath().resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File berhasil disimpan: " + filename);
            return filename;
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Gagal menyimpan file: " + e.getMessage(), e);
        }
    }

    // Hapus file lama jika ada
    public void deleteFile(String filename) {
        if (filename == null || filename.isEmpty() || filename.equals("default.jpg")) {
            return; // Jangan hapus file default
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

    // Dapatkan URL untuk mengakses file
    public String getFileUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "/uploads/default.jpg";
        }
        return "/uploads/" + filename;
    }
}
