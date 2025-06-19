package com.example.flutterbackend.service;

import com.example.flutterbackend.dto.TransactionDTO;
import com.example.flutterbackend.dto.TransactionSummaryDTO;
import com.example.flutterbackend.model.Category;
import com.example.flutterbackend.model.Transaction;
import com.example.flutterbackend.model.User;
import com.example.flutterbackend.repository.CategoryRepository;
import com.example.flutterbackend.repository.TransactionRepository;
import com.example.flutterbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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

    // 1. CREATE: Buat transaksi baru
    public TransactionDTO createTransaction(TransactionDTO dto) {
        try {
            System.out.println("=== TransactionService.createTransaction() called ===");
            
            // Validasi input
            if (dto.getTransactionAmount() == null || dto.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount harus lebih besar dari 0");
            }

            if (dto.getTransactionDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tanggal transaksi wajib diisi");
            }

            if (dto.getCategoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategori wajib dipilih");
            }

            if (dto.getUserId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID wajib diisi");
            }

            // Validasi kategori
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan"));

            // Validasi user
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            
            // Pastikan kategori milik user yang sama
            if (!category.getCreatedBy().getUserId().equals(dto.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kategori tidak dapat digunakan oleh user ini");
            }

            // Buat entitas Transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionAmount(dto.getTransactionAmount());
            transaction.setTransactionDescription(dto.getTransactionDescription());
            transaction.setTransactionDate(dto.getTransactionDate());
            transaction.setCategory(category);
            transaction.setUser(user);
            transaction.setIsDeleted(false);

            // Simpan ke database
            Transaction saved = transactionRepository.save(transaction);
            System.out.println("Transaction created successfully with ID: " + saved.getTransactionId());
            return convertToDTO(saved);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create transaction: " + e.getMessage());
        }
    }

    // 2. READ: Ambil semua transaksi user
    public List<TransactionDTO> getTransactionsByUserId(Long userId) {
        try {
            System.out.println("=== TransactionService.getTransactionsByUserId() called for user: " + userId + " ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            
            List<Transaction> transactions = transactionRepository.findByUserId(userId);
            return transactions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                    
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error getting transactions: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get transactions: " + e.getMessage());
        }
    }

    // 3. READ: Ambil transaksi berdasarkan ID
    public TransactionDTO getTransactionByIdAndUserId(Long transactionId, Long userId) {
        try {
            System.out.println("=== TransactionService.getTransactionByIdAndUserId() called ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaksi tidak ditemukan"));

            // Pastikan transaksi milik user yang benar
            if (!transaction.getUser().getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak memiliki akses ke transaksi ini");
            }

            return convertToDTO(transaction);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error getting transaction by ID: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get transaction: " + e.getMessage());
        }
    }

    // 4. UPDATE: Update transaksi
    public TransactionDTO updateTransaction(Long transactionId, TransactionDTO dto, Long userId) {
        try {
            System.out.println("=== TransactionService.updateTransaction() called ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaksi tidak ditemukan"));

            // Pastikan transaksi milik user yang benar
            if (!transaction.getUser().getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak memiliki akses ke transaksi ini");
            }

            // Update field
            if (dto.getTransactionAmount() != null) {
                if (dto.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount harus lebih besar dari 0");
                }
                transaction.setTransactionAmount(dto.getTransactionAmount());
            }

            if (dto.getTransactionDescription() != null) {
                transaction.setTransactionDescription(dto.getTransactionDescription());
            }

            if (dto.getTransactionDate() != null) {
                transaction.setTransactionDate(dto.getTransactionDate());
            }

            // Update kategori
            if (dto.getCategoryId() != null) {
                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan"));
                
                // Pastikan kategori milik user yang sama
                if (!category.getCreatedBy().getUserId().equals(userId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kategori tidak dapat digunakan oleh user ini");
                }
                
                transaction.setCategory(category);
            }

            Transaction saved = transactionRepository.save(transaction);
            System.out.println("Transaction updated successfully");
            return convertToDTO(saved);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update transaction: " + e.getMessage());
        }
    }

    // 5. DELETE: Soft delete transaksi
    public TransactionDTO deleteTransaction(Long transactionId, Long userId) {
        try {
            System.out.println("=== TransactionService.deleteTransaction() called ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaksi tidak ditemukan"));

            // Pastikan transaksi milik user yang benar
            if (!transaction.getUser().getUserId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak memiliki akses ke transaksi ini");
            }

            transaction.setIsDeleted(true);
            Transaction saved = transactionRepository.save(transaction);
            System.out.println("Transaction deleted successfully");
            return convertToDTO(saved);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete transaction: " + e.getMessage());
        }
    }

    // 6. MONTHLY SUMMARY: Ringkasan transaksi bulan ini
    public TransactionSummaryDTO getMonthlySummaryByUserId(Long userId) {
        try {
            System.out.println("=== TransactionService.getMonthlySummaryByUserId() called for user: " + userId + " ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            
            // Dapatkan tanggal awal dan akhir bulan ini
            YearMonth currentMonth = YearMonth.now();
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();
            
            System.out.println("Calculating summary for period: " + startOfMonth + " to " + endOfMonth);
            
            // Hitung total income dan expense bulan ini
            BigDecimal totalIncome = transactionRepository.sumIncomeByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
            BigDecimal totalExpense = transactionRepository.sumExpenseByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
            
            TransactionSummaryDTO summary = new TransactionSummaryDTO(totalIncome, totalExpense);
            System.out.println("Monthly summary calculated - Income: " + totalIncome + ", Expense: " + totalExpense + ", Balance: " + summary.getNetBalance());
            
            return summary;
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error calculating monthly summary: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to calculate monthly summary: " + e.getMessage());
        }
    }

    // 7. YEARLY SUMMARY: Ringkasan transaksi tahun ini (bonus)
    public TransactionSummaryDTO getYearlySummaryByUserId(Long userId) {
        try {
            System.out.println("=== TransactionService.getYearlySummaryByUserId() called for user: " + userId + " ===");
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
            
            // Dapatkan tanggal awal dan akhir tahun ini
            int currentYear = LocalDate.now().getYear();
            LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
            LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);
            
            // Hitung total income dan expense tahun ini
            BigDecimal totalIncome = transactionRepository.sumIncomeByUserIdAndDateRange(userId, startOfYear, endOfYear);
            BigDecimal totalExpense = transactionRepository.sumExpenseByUserIdAndDateRange(userId, startOfYear, endOfYear);
            
            return new TransactionSummaryDTO(totalIncome, totalExpense);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error calculating yearly summary: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to calculate yearly summary: " + e.getMessage());
        }
    }

    public byte[] generateCsvReport(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

            StringBuilder csvContent = new StringBuilder();
            // CSV Header - Template Laporan (terstruktur)
            csvContent.append("Date,Type,Category,Description,Amount\n");

            // CSV Data - Dinamis berdasarkan data transaksi
            for (Transaction tx : transactions) {
                csvContent.append(tx.getTransactionDate().toString()).append(",");
                csvContent.append(tx.getTransactionType()).append(",");
                String categoryName = "N/A";
                if (tx.getCategory() != null) {
                    categoryName = tx.getCategory().getCategoryName();
                }
                csvContent.append(categoryName.replace(",", ";")).append(","); // Handle commas in category name
                csvContent.append(tx.getTransactionDescription() != null ? tx.getTransactionDescription().replace(",", ";") : "").append(","); // Handle commas in description
                csvContent.append(tx.getTransactionAmount().toPlainString()).append("\n");
            }
            return csvContent.toString().getBytes("UTF-8");
        } catch (Exception e) {
            System.err.println("Error generating CSV report: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate CSV report: " + e.getMessage());
        }
    }

    // Conceptual PDF generation (requires a library like iText or Apache PDFBox)
    // Untuk PDF, Anda memerlukan library seperti iText (com.itextpdf:itextpdf) atau Apache PDFBox (org.apache.pdfbox:pdfbox)
    // yang harus ditambahkan ke pom.xml atau build.gradle Anda.
    // Implementasi di bawah ini adalah placeholder.
    public byte[] generatePdfReport(Long userId, LocalDate startDate, LocalDate endDate) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "PDF generation is not implemented. Requires a PDF library (e.g., iText, Apache PDFBox).");
        /*
        // Contoh konseptual menggunakan iText:
        // Pastikan Anda telah menambahkan dependensi iText ke pom.xml atau build.gradle Anda:
        // Maven:
        // <dependency>
        //     <groupId>com.itextpdf</groupId>
        //     <artifactId>itextpdf</artifactId>
        //     <version>5.5.13.3</version>
        // </dependency>
        // Gradle:
        // implementation 'com.itextpdf:itextpdf:5.5.13.3'

        // import com.itextpdf.text.Document;
        // import com.itextpdf.text.Paragraph;
        // import com.itextpdf.text.pdf.PdfPTable;
        // import com.itextpdf.text.pdf.PdfWriter;

        // try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        //     Document document = new Document();
        //     PdfWriter.getInstance(document, baos);
        //     document.open();
        //     document.add(new Paragraph("Transaction Report from " + startDate + " to " + endDate));
        //     document.add(new Paragraph("User ID: " + userId));
        //     document.add(new Paragraph("\n"));

        //     List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        //     PdfPTable table = new PdfPTable(5); // 5 columns: Date, Type, Category, Description, Amount
        //     table.addCell("Date");
        //     table.addCell("Type");
        //     table.addCell("Category");
        //     table.addCell("Description");
        //     table.addCell("Amount");

        //     for (Transaction tx : transactions) {
        //         table.addCell(tx.getTransactionDate().toString());
        //         table.addCell(tx.getTransactionType());
        //         String categoryName = "N/A";
        //         if (tx.getCategory() != null) {
        //             categoryName = tx.getCategory().getCategoryName();
        //         }
        //         table.addCell(categoryName);
        //         table.addCell(tx.getTransactionDescription() != null ? tx.getTransactionDescription() : "");
        //         table.addCell(tx.getTransactionAmount().toPlainString());
        //     }
        //     document.add(table);
        //     document.close();
        //     return baos.toByteArray();
        // } catch (Exception e) {
        //     System.err.println("Error generating PDF report: " + e.getMessage());
        //     e.printStackTrace();
        //     throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF report: " + e.getMessage());
        // }
        */
    }
}
