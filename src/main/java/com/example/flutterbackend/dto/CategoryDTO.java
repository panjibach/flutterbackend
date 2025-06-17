package com.example.flutterbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
public class CategoryDTO {

    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("categoryName")
    @NotBlank(message = "Category name is required")
    private String categoryName;

    @JsonProperty("isExpense")
    @NotNull(message = "Expense flag is required")
    private Boolean isExpense;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    @JsonProperty("createdByUserId")
    private Long createdByUserId;

    // Constructor kosong
    public CategoryDTO() {}

    // Constructor dengan parameter
    public CategoryDTO(String categoryName, Boolean isExpense) {
        this.categoryName = categoryName;
        this.isExpense = isExpense;
    }

    public CategoryDTO(Long categoryId, String categoryName, Boolean isExpense, Boolean isDeleted, Long createdByUserId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isExpense = isExpense;
        this.isDeleted = isDeleted;
        this.createdByUserId = createdByUserId;
    }

    // Getters dan Setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getIsExpense() {
        return isExpense;
    }

    public void setIsExpense(Boolean isExpense) {
        this.isExpense = isExpense;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
