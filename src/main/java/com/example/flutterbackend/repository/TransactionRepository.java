package com.example.flutterbackend.repository;

import com.example.flutterbackend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Ambil semua transaksi berdasarkan userId (yang tidak dihapus)
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.isDeleted = false ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    // Sum income berdasarkan user dan rentang tanggal (category.isExpense = false)
    @Query("SELECT COALESCE(SUM(t.transactionAmount), 0.0) FROM Transaction t WHERE t.user.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate AND t.category.isExpense = false AND t.isDeleted = false")
    BigDecimal sumIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Sum expense berdasarkan user dan rentang tanggal (category.isExpense = true)
    @Query("SELECT COALESCE(SUM(t.transactionAmount), 0.0) FROM Transaction t WHERE t.user.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate AND t.category.isExpense = true AND t.isDeleted = false")
    BigDecimal sumExpenseByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Query untuk mendapatkan total transaksi berdasarkan user (untuk summary keseluruhan)
    @Query("SELECT COALESCE(SUM(t.transactionAmount), 0.0) FROM Transaction t WHERE t.user.userId = :userId AND t.category.isExpense = false AND t.isDeleted = false")
    BigDecimal sumIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.transactionAmount), 0.0) FROM Transaction t WHERE t.user.userId = :userId AND t.category.isExpense = true AND t.isDeleted = false")
    BigDecimal sumExpenseByUserId(@Param("userId") Long userId);

    // Query untuk mendapatkan transaksi berdasarkan user dan kategori
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.category.categoryId = :categoryId AND t.isDeleted = false ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    // Query untuk mendapatkan transaksi berdasarkan rentang tanggal dan user
    // Ini adalah metode yang akan digunakan untuk laporan CSV/PDF
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate AND t.isDeleted = false ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Query untuk mendapatkan transaksi terbaru berdasarkan user (untuk dashboard)
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND t.isDeleted = false ORDER BY t.transactionDate DESC, t.transactionId DESC")
    List<Transaction> findRecentTransactionsByUserId(@Param("userId") Long userId);

    
}
