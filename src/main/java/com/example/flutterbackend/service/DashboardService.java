package com.example.flutterbackend.service;

import com.example.flutterbackend.dto.DashboardDTO;
import com.example.flutterbackend.dto.TransactionDTO;
import com.example.flutterbackend.dto.TransactionSummaryDTO;
import com.example.flutterbackend.model.Transaction;
import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.TransactionRepository;
import com.example.flutterbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Konversi Transaction ke TransactionDTO
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionAmount(transaction.getTransactionAmount());
        dto.setTransactionDescription(transaction.getTransactionDescription());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setCategoryId(transaction.getCategory() != null ? transaction.getCategory().getCategoryId() : null);
        dto.setUserId(transaction.getUser() != null ? transaction.getUser().getUserId() : null);
        dto.setIsDeleted(transaction.getIsDeleted());
        dto.setTransactionType(transaction.getTransactionType());
        return dto;
    }

    // Dashboard data untuk user (dipanggil saat login)
    public DashboardDTO getDashboardData(Long userId) {
        try {
            System.out.println("=== DashboardService.getDashboardData() called for user: " + userId + " ===");
            
            // Validasi user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

            // 1. Dapatkan ringkasan bulanan
            TransactionSummaryDTO monthlySummary = transactionService.getMonthlySummaryByUserId(userId);

            // 2. Dapatkan 5 transaksi terbaru
            List<Transaction> recentTransactions = transactionRepository.findRecentTransactionsByUserId(userId)
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            List<TransactionDTO> recentTransactionDTOs = recentTransactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // 3. Hitung total transaksi user
            Long totalTransactions = (long) transactionRepository.findByUserId(userId).size();

            // 4. Buat dashboard DTO
            DashboardDTO dashboard = new DashboardDTO(monthlySummary, recentTransactionDTOs, totalTransactions);
            
            System.out.println("Dashboard data prepared successfully");
            return dashboard;
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error getting dashboard data: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get dashboard data: " + e.getMessage());
        }
    }
}
