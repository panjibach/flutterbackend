package com.example.flutterbackend.Controller;

import com.example.flutterbackend.dto.CategoryDTO;
import com.example.flutterbackend.service.CategoryService;
import com.example.flutterbackend.security.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthenticationHelper authHelper;
    
    // GET semua kategori berdasarkan user (hanya yang tidak di-soft delete)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kategori user ini");
        }
        
        List<CategoryDTO> categories = categoryService.getCategoriesByUserId(userId);
        return ResponseEntity.ok(categories);
    }
    
    // GET semua kategori termasuk yang sudah di-soft delete berdasarkan user
    @GetMapping("/all-including-deleted/user/{userId}")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesIncludingDeletedByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kategori user ini");
        }
        
        List<CategoryDTO> categories = categoryService.getAllCategoriesIncludingDeletedByUserId(userId);
        return ResponseEntity.ok(categories);
    }
    
    // GET hanya kategori yang sudah di-soft delete berdasarkan user
    @GetMapping("/deleted/user/{userId}")
    public ResponseEntity<List<CategoryDTO>> getDeletedCategoriesByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kategori user ini");
        }
        
        List<CategoryDTO> deletedCategories = categoryService.getDeletedCategoriesByUserId(userId);
        return ResponseEntity.ok(deletedCategories);
    }

    // POST tambah kategori
    @PostMapping("/user/{userId}")
    public ResponseEntity<CategoryDTO> createCategory(
            @PathVariable Long userId, 
            @Valid @RequestBody CategoryDTO categoryDTO,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk membuat kategori untuk user ini");
        }
        
        try {
            CategoryDTO saved = categoryService.createCategory(categoryDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error creating category: " + e.getMessage(), e);
        }
    }

    // DELETE kategori berdasarkan ID (soft delete)
    @DeleteMapping("/{categoryId}/user/{userId}")
    public ResponseEntity<CategoryDTO> deleteCategory(
            @PathVariable Long categoryId, 
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk menghapus kategori ini");
        }
        
        CategoryDTO deletedCategory = categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.ok(deletedCategory);
    }
    
    // PUT untuk mengembalikan (restore) kategori yang sudah di-soft delete
    @PutMapping("/restore/{categoryId}/user/{userId}")
    public ResponseEntity<CategoryDTO> restoreCategory(
            @PathVariable Long categoryId, 
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk restore kategori ini");
        }
        
        CategoryDTO restoredCategory = categoryService.restoreCategory(categoryId, userId);
        return ResponseEntity.ok(restoredCategory);
    }

    // PUT update kategori berdasarkan ID
    @PutMapping("/{categoryId}/user/{userId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long categoryId, 
            @PathVariable Long userId, 
            @Valid @RequestBody CategoryDTO categoryDTO,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk mengupdate kategori ini");
        }
        
        CategoryDTO updated = categoryService.updateCategory(categoryId, categoryDTO, userId);
        return ResponseEntity.ok(updated);
    }

    // GET kategori income berdasarkan user
    @GetMapping("/income/user/{userId}")
    public ResponseEntity<List<CategoryDTO>> getIncomeCategoriesByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kategori income user ini");
        }
        
        List<CategoryDTO> categories = categoryService.getIncomeCategoriesByUserId(userId);
        return ResponseEntity.ok(categories);
    }

    // GET kategori expense berdasarkan user
    @GetMapping("/expense/user/{userId}")
    public ResponseEntity<List<CategoryDTO>> getExpenseCategoriesByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        if (!authHelper.validateUserAccess(userId, request)) {
            throw new RuntimeException("Anda tidak memiliki akses ke kategori expense user ini");
        }
        
        List<CategoryDTO> categories = categoryService.getExpenseCategoriesByUserId(userId);
        return ResponseEntity.ok(categories);
    }

    // Metode untuk menangani exception
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
