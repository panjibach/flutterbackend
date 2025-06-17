package com.example.flutterbackend.repository;

import com.example.flutterbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Cari user berdasarkan email
    Optional<User> findByUserEmail(String userEmail);

    // Cari user berdasarkan nama
    Optional<User> findByUserName(String userName);

    // Cari semua user yang aktif (tidak dihapus)
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllActiveUsers();

    // Cari semua user yang dihapus
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findAllDeletedUsers();

    // Cari user berdasarkan email dan password (untuk login)
    @Query("SELECT u FROM User u WHERE u.userEmail = :userEmail AND u.userPassword = :userPassword AND u.isDeleted = false")
    Optional<User> findByUserEmailAndUserPassword(@Param("userEmail") String userEmail, @Param("userPassword") String userPassword);

    // Cek apakah email sudah ada (untuk validasi registrasi)
    boolean existsByUserEmail(String userEmail);

    // Cari user berdasarkan email (termasuk yang dihapus)
    @Query("SELECT u FROM User u WHERE u.userEmail = :userEmail")
    Optional<User> findByUserEmailIncludeDeleted(@Param("userEmail") String userEmail);

    // Cari user berdasarkan nama (case insensitive)
    @Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :userName, '%')) AND u.isDeleted = false")
    List<User> findByUserNameContainingIgnoreCase(@Param("userName") String userName);
}
