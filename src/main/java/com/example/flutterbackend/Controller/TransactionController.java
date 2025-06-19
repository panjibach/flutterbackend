package com.example.flutterbackend.Controller;

import com.example.flutterbackend.dto.TransactionDTO;
import com.example.flutterbackend.dto.TransactionSummaryDTO;
import com.example.flutterbackend.service.TransactionService;
import com.example.flutterbackend.security.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
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

    // ========== EXPORT ENDPOINTS ==========

    // Endpoint baru untuk ekspor CSV bulanan
    @GetMapping(value = "/export/csv/monthly/user/{userId}", produces = "text/csv")
    public ResponseEntity<byte[]> exportMonthlyCsv(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        try {
            System.out.println("=== GET /api/transactions/export/csv/monthly/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Anda tidak memiliki akses untuk mengekspor laporan ini".getBytes());
            }

            YearMonth targetMonth;
            if (year != null && month != null) {
                targetMonth = YearMonth.of(year, month);
            } else {
                targetMonth = YearMonth.now();
            }

            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            byte[] csvBytes = transactionService.generateCsvReport(userId, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "monthly_transactions_" + targetMonth.toString() + ".csv");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error exporting monthly CSV: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Failed to export monthly CSV: " + e.getMessage()).getBytes());
        }
    }

    // Endpoint baru untuk ekspor PDF bulanan (konseptual)
    @GetMapping(value = "/export/pdf/monthly/user/{userId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportMonthlyPdf(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        try {
            System.out.println("=== GET /api/transactions/export/pdf/monthly/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Anda tidak memiliki akses untuk mengekspor laporan ini".getBytes());
            }

            YearMonth targetMonth;
            if (year != null && month != null) {
                targetMonth = YearMonth.of(year, month);
            } else {
                targetMonth = YearMonth.now();
            }

            LocalDate startDate = targetMonth.atDay(1);
            LocalDate endDate = targetMonth.atEndOfMonth();

            // Ini akan melempar NOT_IMPLEMENTED jika metode service adalah placeholder
            byte[] pdfBytes = transactionService.generatePdfReport(userId, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDispositionFormData("attachment", "monthly_transactions_" + targetMonth.toString() + ".pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error exporting monthly PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Failed to export monthly PDF: " + e.getMessage()).getBytes());
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
