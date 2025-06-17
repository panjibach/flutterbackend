package com.example.flutterbackend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "token", length = 1000)
    private String token;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "expiry_date")
    private Date expiryDate;
    
    @Column(name = "blacklisted_at")
    private Date blacklistedAt;
    
    // Constructors
    public TokenBlacklist() {}
    
    public TokenBlacklist(String token, Long userId, Date expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.blacklistedAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Date getBlacklistedAt() {
        return blacklistedAt;
    }
    
    public void setBlacklistedAt(Date blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }
}
