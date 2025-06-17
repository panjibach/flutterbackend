package com.example.flutterbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TransactionDTO {
    
    @JsonProperty("transactionId")
    private Long transactionId;

    @JsonProperty("transactionAmount")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal transactionAmount;
    
    @JsonProperty("transactionDescription")
    private String transactionDescription;
    
    @JsonProperty("transactionDate")
    @NotNull(message = "Date is required")
    private LocalDate transactionDate;
    
    @JsonProperty("categoryId")
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    @JsonProperty("transactionType")
    private String transactionType;

    // Constructor kosong
    public TransactionDTO() {}

    // Constructor dengan parameter
    public TransactionDTO(BigDecimal transactionAmount, String transactionDescription, LocalDate transactionDate, Long categoryId) {
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
        this.transactionDate = transactionDate;
        this.categoryId = categoryId;
    }

    public TransactionDTO(Long transactionId, BigDecimal transactionAmount, String transactionDescription, 
                        LocalDate transactionDate, Long categoryId, Long userId) {
        this.transactionId = transactionId;
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
        this.transactionDate = transactionDate;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    // Getters dan Setters
    public Long getTransactionId() { 
        return transactionId; 
    }
    
    public void setTransactionId(Long transactionId) { 
        this.transactionId = transactionId; 
    }
    
    public BigDecimal getTransactionAmount() { 
        return transactionAmount; 
    }
    
    public void setTransactionAmount(BigDecimal transactionAmount) { 
        this.transactionAmount = transactionAmount; 
    }
    
    public String getTransactionDescription() { 
        return transactionDescription; 
    }
    
    public void setTransactionDescription(String transactionDescription) { 
        this.transactionDescription = transactionDescription; 
    }
    
    public LocalDate getTransactionDate() { 
        return transactionDate; 
    }
    
    public void setTransactionDate(LocalDate transactionDate) { 
        this.transactionDate = transactionDate; 
    }
    
    public Long getCategoryId() { 
        return categoryId; 
    }
    
    public void setCategoryId(Long categoryId) { 
        this.categoryId = categoryId; 
    }
    
    public Long getUserId() { 
        return userId; 
    }
    
    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
