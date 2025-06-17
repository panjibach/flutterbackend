package com.example.flutterbackend.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@SQLDelete(sql = "UPDATE categories SET is_deleted = true WHERE category_id = ?")
@Where(clause = "is_deleted = false")
@Table(name = "categories")
@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class, 
  property = "categoryId")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @JsonProperty("categoryId")
    private Long categoryId;

    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Column(name = "category_name")
    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("isExpense")
    @Column(name = "is_expense")
    private Boolean isExpense; // true jika kategori adalah pengeluaran, false jika pemasukan

    @Column(name = "is_deleted")
    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    // Relasi one-to-many dengan Transaction
    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> categoryTransactions = new ArrayList<>();

    // Relasi many-to-one dengan User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore // Hindari circular reference saat serialisasi
    private User createdBy;

    // Konstruktor kosong diperlukan oleh JPA
    public Category() {}

    // Konstruktor dengan parameter
    public Category(String categoryName, Boolean isExpense) {
        this.categoryName = categoryName;
        this.isExpense = isExpense;
    }

    public Category(String categoryName, Boolean isExpense, User createdBy) {
        this.categoryName = categoryName;
        this.isExpense = isExpense;
        this.createdBy = createdBy;
    }

    // Getters & Setters
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

    @JsonProperty("isExpense")
    public Boolean getIsExpense() {
        return isExpense;
    }

    @JsonProperty("isExpense")
    public void setIsExpense(Boolean isExpense) {
        this.isExpense = isExpense;
    }

    public List<Transaction> getCategoryTransactions() {
        return categoryTransactions;
    }

    public void setCategoryTransactions(List<Transaction> categoryTransactions) {
        this.categoryTransactions = categoryTransactions;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public User getCreatedBy() { 
        return createdBy; 
    }

    public void setCreatedBy(User createdBy) { 
        this.createdBy = createdBy; 
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", isExpense=" + isExpense +
                ", isDeleted=" + isDeleted +
                ", createdBy=" + (createdBy != null ? createdBy.getUserId() : null) +
                '}';
    }
}
