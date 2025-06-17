package com.example.flutterbackend.repository;

import com.example.flutterbackend.model.Category;
import com.example.flutterbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Cari kategori berdasarkan nama dan user yang membuat (yang tidak dihapus)
    @Query("SELECT c FROM Category c WHERE c.categoryName = :categoryName AND c.createdBy = :user AND c.isDeleted = false")
    Category findByCategoryNameAndCreatedBy(@Param("categoryName") String categoryName, @Param("user") User user);

    // Cari kategori berdasarkan nama, user, dan status deleted
    Optional<Category> findByCategoryNameAndCreatedByAndIsDeleted(String categoryName, User createdBy, Boolean isDeleted);

    // Cari kategori berdasarkan ID dan user yang membuat
    Optional<Category> findByCategoryIdAndCreatedBy(Long categoryId, User createdBy);

    // Cari semua kategori berdasarkan user yang membuat
    List<Category> findByCreatedBy(User createdBy);

    // Cari kategori berdasarkan user dan status deleted
    List<Category> findByCreatedByAndIsDeleted(User createdBy, Boolean isDeleted);

    // Cari kategori berdasarkan user, tipe expense, dan status deleted
    List<Category> findByCreatedByAndIsExpenseAndIsDeleted(User createdBy, Boolean isExpense, Boolean isDeleted);

    // Query native SQL untuk mendapatkan semua kategori termasuk yang di-soft delete
    @Query(value = "SELECT * FROM categories", nativeQuery = true)
    List<Category> findAllIncludingDeleted();

    // Query native SQL untuk mendapatkan hanya kategori yang sudah di-soft delete
    @Query(value = "SELECT * FROM categories WHERE is_deleted = true", nativeQuery = true)
    List<Category> findAllDeleted();

    // Query native SQL untuk mendapatkan kategori berdasarkan ID termasuk yang di-soft delete
    @Query(value = "SELECT * FROM categories WHERE category_id = :categoryId", nativeQuery = true)
    Optional<Category> findByIdIncludeDeleted(@Param("categoryId") Long categoryId);

    // Cari kategori income berdasarkan user (isExpense = false)
    @Query("SELECT c FROM Category c WHERE c.createdBy = :user AND c.isExpense = false AND c.isDeleted = false")
    List<Category> findIncomeCategoriesByUser(@Param("user") User user);

    // Cari kategori expense berdasarkan user (isExpense = true)
    @Query("SELECT c FROM Category c WHERE c.createdBy = :user AND c.isExpense = true AND c.isDeleted = false")
    List<Category> findExpenseCategoriesByUser(@Param("user") User user);

    // Cari kategori berdasarkan user dan nama (case insensitive)
    @Query("SELECT c FROM Category c WHERE c.createdBy = :user AND LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%')) AND c.isDeleted = false")
    List<Category> findByCreatedByAndCategoryNameContainingIgnoreCase(@Param("user") User user, @Param("categoryName") String categoryName);
}
