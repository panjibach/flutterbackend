package com.example.flutterbackend.dto;

import java.math.BigDecimal;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TransactionSummaryDTO {
    
    @JsonProperty("totalIncome")
    private BigDecimal totalIncome;
    
    @JsonProperty("totalExpense")
    private BigDecimal totalExpense;
    
    @JsonProperty("netBalance")
    private BigDecimal netBalance;

    // Constructor kosong
    public TransactionSummaryDTO() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpense = BigDecimal.ZERO;
        this.netBalance = BigDecimal.ZERO;
    }

    // Constructor dengan parameter
    public TransactionSummaryDTO(BigDecimal totalIncome, BigDecimal totalExpense) {
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        this.netBalance = this.totalIncome.subtract(this.totalExpense);
    }

    // Getters dan Setters
    public BigDecimal getTotalIncome() { 
        return totalIncome; 
    }
    
    public void setTotalIncome(BigDecimal totalIncome) { 
        this.totalIncome = totalIncome;
        updateNetBalance();
    }
    
    public BigDecimal getTotalExpense() { 
        return totalExpense; 
    }
    
    public void setTotalExpense(BigDecimal totalExpense) { 
        this.totalExpense = totalExpense;
        updateNetBalance();
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    // Helper method untuk update net balance
    private void updateNetBalance() {
        if (totalIncome != null && totalExpense != null) {
            this.netBalance = totalIncome.subtract(totalExpense);
        }
    }
}
