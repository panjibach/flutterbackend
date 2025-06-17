package com.example.flutterbackend.Controller;

import com.example.flutterbackend.dto.TransactionDTO;
import com.example.flutterbackend.dto.TransactionSummaryDTO;
import com.example.flutterbackend.service.TransactionService;
import com.example.flutterbackend.security.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(
    value = "/api/transactions",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthenticationHelper authHelper;

    // ========== CRUD TRANSAKSI ==========

    // 1. CREATE: Buat transaksi baru
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionDTO> createTransaction(
            @Valid @RequestBody TransactionDTO transactionDTO,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== POST /api/transactions called ===");
            
            // Validasi bahwa user hanya bisa membuat transaksi untuk dirinya sendiri
            Long authenticatedUserId = authHelper.getUserIdFromRequest(request);
            if (transactionDTO.getUserId() != null && !transactionDTO.getUserId().equals(authenticatedUserId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda hanya bisa membuat transaksi untuk diri sendiri");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            // Set userId dari authenticated user jika belum di-set
            if (transactionDTO.getUserId() == null) {
                transactionDTO.setUserId(authenticatedUserId);
            }
            
            TransactionDTO savedTransaction = transactionService.createTransaction(transactionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
            
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            throw e;
        }
    }

    // 2. READ: Ambil semua transaksi user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== GET /api/transactions/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke transaksi user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            List<TransactionDTO> transactions = transactionService.getTransactionsByUserId(userId);
            return ResponseEntity.ok(transactions);
            
        } catch (Exception e) {
            System.err.println("Error getting transactions: " + e.getMessage());
            throw e;
        }
    }

    // 3. READ: Ambil transaksi berdasarkan ID
    @GetMapping("/{transactionId}/user/{userId}")
    public ResponseEntity<TransactionDTO> getTransactionById(
            @PathVariable Long transactionId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== GET /api/transactions/" + transactionId + "/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke transaksi user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            TransactionDTO transaction = transactionService.getTransactionByIdAndUserId(transactionId, userId);
            return ResponseEntity.ok(transaction);
            
        } catch (Exception e) {
            System.err.println("Error getting transaction: " + e.getMessage());
            throw e;
        }
    }

    // 4. UPDATE: Update transaksi
    @PutMapping("/{transactionId}/user/{userId}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long transactionId, 
            @PathVariable Long userId, 
            @Valid @RequestBody TransactionDTO transactionDTO,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== PUT /api/transactions/" + transactionId + "/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk mengupdate transaksi ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            TransactionDTO updatedTransaction = transactionService.updateTransaction(transactionId, transactionDTO, userId);
            return ResponseEntity.ok(updatedTransaction);
            
        } catch (Exception e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            throw e;
        }
    }

    // 5. DELETE: Hapus transaksi (soft delete)
    @DeleteMapping("/{transactionId}/user/{userId}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long transactionId, 
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== DELETE /api/transactions/" + transactionId + "/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses untuk menghapus transaksi ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            TransactionDTO deletedTransaction = transactionService.deleteTransaction(transactionId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Transaksi berhasil dihapus");
            response.put("transaction", deletedTransaction);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========== SUMMARY ENDPOINTS ==========

    // 6. MONTHLY SUMMARY: Ringkasan bulan ini (untuk dashboard)
    @GetMapping("/summary/monthly/user/{userId}")
    public ResponseEntity<TransactionSummaryDTO> getMonthlySummary(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== GET /api/transactions/summary/monthly/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke ringkasan transaksi user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            TransactionSummaryDTO summary = transactionService.getMonthlySummaryByUserId(userId);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            System.err.println("Error getting monthly summary: " + e.getMessage());
            throw e;
        }
    }

    // 7. YEARLY SUMMARY: Ringkasan tahun ini (bonus)
    @GetMapping("/summary/yearly/user/{userId}")
    public ResponseEntity<TransactionSummaryDTO> getYearlySummary(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== GET /api/transactions/summary/yearly/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke ringkasan transaksi user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            TransactionSummaryDTO summary = transactionService.getYearlySummaryByUserId(userId);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            System.err.println("Error getting yearly summary: " + e.getMessage());
            throw e;
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
