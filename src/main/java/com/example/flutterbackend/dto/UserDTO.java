package com.example.flutterbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class UserDTO {
    
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userName")
    @NotBlank(message = "Nama tidak boleh kosong")
    private String userName;

    @JsonProperty("userEmail")
    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    private String userEmail;

    // Field internal untuk menyimpan nama file (tidak di-expose ke JSON)
    @JsonIgnore
    private String userProfile;

    // Field yang di-expose ke JSON untuk URL akses file
    @JsonProperty("profileUrl")
    private String profileUrl;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = false;

    // Constructor kosong
    public UserDTO() {}

    // Constructor dengan parameter
    public UserDTO(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public UserDTO(Long userId, String userName, String userEmail, String userProfile) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfile = userProfile;
    }

    public UserDTO(Long userId, String userName, String userEmail, String userProfile, Boolean isDeleted) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfile = userProfile;
        this.isDeleted = isDeleted;
    }

    // Getters dan Setters
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

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
