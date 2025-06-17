package com.example.flutterbackend.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String userEmail;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private Map<String, Object> attributes;
    
    public UserSession(Long userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.loginTime = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.attributes = new HashMap<>();
    }
    
    // Update last activity time
    public void updateLastActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }
    
    // Add attribute
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    // Get attribute
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }
    
    // Remove attribute
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }
    
    // Check if session is active (within 1 hour of last activity)
    public boolean isActive() {
        return this.lastActivityTime.plusHours(1).isAfter(LocalDateTime.now());
    }
    
    // Getters and setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
