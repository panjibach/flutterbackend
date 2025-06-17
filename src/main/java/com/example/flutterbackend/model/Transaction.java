package com.example.flutterbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "transactions")
@SQLDelete(sql = "UPDATE transactions SET is_deleted = true WHERE transaction_id = ?")
@Where(clause = "is_deleted = false")
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "transactionId")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    @JsonProperty("transactionId")
    private Long transactionId;

    @NotNull(message = "Amount tidak boleh null")
    @Positive(message = "Amount harus positif")
    @Column(name = "transaction_amount", precision = 19, scale = 2)
    @JsonProperty("transactionAmount")
    private BigDecimal transactionAmount;

    @Column(name = "transaction_description")
    @JsonProperty("transactionDescription")
    private String transactionDescription;

    @NotNull(message = "Date tidak boleh null")
    @Column(name = "transaction_date")
    @JsonProperty("transactionDate")
    private LocalDate transactionDate;

    @Column(name = "is_deleted")
    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    // Relasi many-to-one dengan Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category tidak boleh null")
    private Category category;

    // Relasi many-to-one dengan User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Konstruktor kosong diperlukan oleh JPA
    public Transaction() {}

    // Konstruktor dengan parameter
    public Transaction(BigDecimal transactionAmount, String transactionDescription, LocalDate transactionDate, Category category) {
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
        this.transactionDate = transactionDate;
        this.category = category;
    }

    public Transaction(BigDecimal transactionAmount, String transactionDescription, LocalDate transactionDate, Category category, User user) {
        this.transactionAmount = transactionAmount;
        this.transactionDescription = transactionDescription;
        this.transactionDate = transactionDate;
        this.category = category;
        this.user = user;
    }

    // Getters & Setters
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    // Helper method untuk mendapatkan tipe transaksi
    @JsonProperty("transactionType")
    public String getTransactionType() {
        if (category != null && category.getIsExpense() != null) {
            return category.getIsExpense() ? "EXPENSE" : "INCOME";
        }
        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", transactionAmount=" + transactionAmount +
                ", transactionDescription='" + transactionDescription + '\'' +
                ", transactionDate=" + transactionDate +
                ", isDeleted=" + isDeleted +
                ", category=" + (category != null ? category.getCategoryId() : null) +
                ", user=" + (user != null ? user.getUserId() : null) +
                '}';
    }
}
