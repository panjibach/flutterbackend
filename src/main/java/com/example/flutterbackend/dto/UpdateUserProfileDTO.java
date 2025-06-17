package com.example.flutterbackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UpdateUserProfileDTO {
    
    @JsonProperty("userName")
    @Size(min = 2, max = 50, message = "Nama harus antara 2-50 karakter")
    private String userName;
    
    @JsonProperty("userProfile")
    private String userProfile;

    // Constructor kosong
    public UpdateUserProfileDTO() {}

    // Constructor dengan parameter
    public UpdateUserProfileDTO(String userName, String userProfile) {
        this.userName = userName;
        this.userProfile = userProfile;
    }

    // Getters dan Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
}
