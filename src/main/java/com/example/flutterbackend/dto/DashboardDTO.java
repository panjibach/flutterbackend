package com.example.flutterbackend.dto;

import java.util.List;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DashboardDTO {
    
    @JsonProperty("monthlySummary")
    private TransactionSummaryDTO monthlySummary;
    
    @JsonProperty("recentTransactions")
    private List<TransactionDTO> recentTransactions;
    
    @JsonProperty("totalTransactions")
    private Long totalTransactions;

    // Constructor kosong
    public DashboardDTO() {}

    // Constructor dengan parameter
    public DashboardDTO(TransactionSummaryDTO monthlySummary, List<TransactionDTO> recentTransactions, Long totalTransactions) {
        this.monthlySummary = monthlySummary;
        this.recentTransactions = recentTransactions;
        this.totalTransactions = totalTransactions;
    }

    // Getters dan Setters
    public TransactionSummaryDTO getMonthlySummary() {
        return monthlySummary;
    }

    public void setMonthlySummary(TransactionSummaryDTO monthlySummary) {
        this.monthlySummary = monthlySummary;
    }

    public List<TransactionDTO> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
}
