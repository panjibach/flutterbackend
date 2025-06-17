package com.example.flutterbackend.service;

import com.example.flutterbackend.dto.CategoryDTO;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Konversi Category entity ke CategoryDTO
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setIsExpense(category.getIsExpense());
        dto.setIsDeleted(category.getIsDeleted());
        if (category.getCreatedBy() != null) {
            dto.setCreatedByUserId(category.getCreatedBy().getUserId());
        }
        return dto;
    }

    // Ambil semua kategori berdasarkan user (yang belum dihapus)
    public List<CategoryDTO> getCategoriesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        
        List<Category> categories = categoryRepository.findByCreatedByAndIsDeleted(user, false);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Ambil semua kategori termasuk yang sudah di-soft delete berdasarkan user
    public List<CategoryDTO> getAllCategoriesIncludingDeletedByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        
        List<Category> categories = categoryRepository.findByCreatedBy(user);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Ambil kategori yang sudah dihapus berdasarkan user
    public List<CategoryDTO> getDeletedCategoriesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        
        List<Category> deletedCategories = categoryRepository.findByCreatedByAndIsDeleted(user, true);
        return deletedCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Buat kategori baru
    public CategoryDTO createCategory(CategoryDTO categoryDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        // Validasi input
        if (categoryDTO.getCategoryName() == null || categoryDTO.getCategoryName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama kategori wajib diisi");
        }

        if (categoryDTO.getIsExpense() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipe kategori (expense/income) wajib diisi");
        }

        // Cek apakah kategori dengan nama yang sama sudah ada untuk user ini
        Optional<Category> existingCategory = categoryRepository.findByCategoryNameAndCreatedByAndIsDeleted(
                categoryDTO.getCategoryName(), user, false);
        
        if (existingCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kategori dengan nama tersebut sudah ada");
        }

        Category category = new Category();
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setIsExpense(categoryDTO.getIsExpense());
        category.setIsDeleted(false);
        category.setCreatedBy(user);

        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    // Ambil kategori berdasarkan ID dan user
    public CategoryDTO getCategoryByIdAndUserId(Long categoryId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Category category = categoryRepository.findByCategoryIdAndCreatedBy(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan atau tidak memiliki akses"));

        return convertToDTO(category);
    }

    // Soft delete kategori
    public CategoryDTO deleteCategory(Long categoryId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Category category = categoryRepository.findByCategoryIdAndCreatedBy(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan atau tidak memiliki akses"));

        // Soft delete semua transaksi yang terkait dengan kategori ini
        for (Transaction transaction : category.getCategoryTransactions()) {
            if (!transaction.getIsDeleted()) {
                transaction.setIsDeleted(true);
                transactionRepository.save(transaction);
            }
        }

        // Soft delete kategori
        category.setIsDeleted(true);
        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    // Restore kategori
    public CategoryDTO restoreCategory(Long categoryId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Category category = categoryRepository.findByCategoryIdAndCreatedBy(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan atau tidak memiliki akses"));

        // Restore kategori
        category.setIsDeleted(false);
        Category saved = categoryRepository.save(category);

        // Restore semua transaksi yang terkait dengan kategori ini
        for (Transaction transaction : category.getCategoryTransactions()) {
            if (transaction.getIsDeleted()) {
                transaction.setIsDeleted(false);
                transactionRepository.save(transaction);
            }
        }

        return convertToDTO(saved);
    }

    // Update kategori
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Category category = categoryRepository.findByCategoryIdAndCreatedBy(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan atau tidak memiliki akses"));

        // Validasi input
        if (categoryDTO.getCategoryName() != null && !categoryDTO.getCategoryName().trim().isEmpty()) {
            // Cek apakah nama kategori baru sudah ada untuk user ini (kecuali kategori yang sedang diupdate)
            Optional<Category> existingCategory = categoryRepository.findByCategoryNameAndCreatedByAndIsDeleted(
                    categoryDTO.getCategoryName(), user, false);
            
            if (existingCategory.isPresent() && !existingCategory.get().getCategoryId().equals(categoryId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kategori dengan nama tersebut sudah ada");
            }
            
            category.setCategoryName(categoryDTO.getCategoryName());
        }

        if (categoryDTO.getIsExpense() != null) {
            category.setIsExpense(categoryDTO.getIsExpense());
        }

        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    // Ambil kategori income berdasarkan user
    public List<CategoryDTO> getIncomeCategoriesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        
        List<Category> categories = categoryRepository.findByCreatedByAndIsExpenseAndIsDeleted(user, false, false);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Ambil kategori expense berdasarkan user
    public List<CategoryDTO> getExpenseCategoriesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        
        List<Category> categories = categoryRepository.findByCreatedByAndIsExpenseAndIsDeleted(user, true, false);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
