package com.example.flutterbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE user_id = ?")
@Where(clause = "is_deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @JsonProperty("userId")
    private Long userId;
    
    @NotBlank(message = "Nama tidak boleh kosong")
    @Column(name = "user_name")
    @JsonProperty("userName")
    private String userName;
    
    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Column(name = "user_email", unique = true)
    @JsonProperty("userEmail")
    private String userEmail;
    
    @NotBlank(message = "Password tidak boleh kosong")
    @Column(name = "user_password")
    @JsonIgnore // Jangan expose password di JSON response
    private String userPassword;
    
    @Column(name = "user_profile")
    @JsonProperty("userProfile")
    private String userProfile;
    
    @Column(name = "is_deleted")
    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    // Relasi one-to-many dengan Transaction
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Hindari circular reference
    private List<Transaction> userTransactions = new ArrayList<>();

    // Relasi one-to-many dengan Category
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Hindari circular reference
    private List<Category> userCategories = new ArrayList<>();

    // Konstruktor default
    public User() {}

    // Konstruktor dengan parameter
    public User(String userName, String userEmail, String userPassword) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    // Getters & Setters
    public Long getUserId() { 
        return userId; 
    }

    public void setUserId(Long userId) { 
        this.userId = userId; 
    }

    public String getUserName() { 
        return userName; 
    }

    public void setUserName(String userName) { 
        this.userName = userName; 
    }

    public String getUserEmail() { 
        return userEmail; 
    }

    public void setUserEmail(String userEmail) { 
        this.userEmail = userEmail; 
    }

    public String getUserProfile() { 
        return userProfile; 
    }

    public void setUserProfile(String userProfile) { 
        this.userProfile = userProfile; 
    }

    public Boolean getIsDeleted() { 
        return isDeleted; 
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted; 
    }

    public List<Transaction> getUserTransactions() { 
        return userTransactions; 
    }

    public void setUserTransactions(List<Transaction> userTransactions) { 
        this.userTransactions = userTransactions; 
    }

    public List<Category> getUserCategories() {
        return userCategories;
    }

    public void setUserCategories(List<Category> userCategories) {
        this.userCategories = userCategories;
    }

    public String getUserPassword() { 
        return userPassword; 
    }

    public void setUserPassword(String userPassword) { 
        this.userPassword = userPassword; 
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userProfile='" + userProfile + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
