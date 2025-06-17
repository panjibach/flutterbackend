package com.example.flutterbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UserRegistrationDTO {
    
    @JsonProperty("userName")
    @NotBlank(message = "Nama wajib diisi")
    @Size(min = 2, max = 50, message = "Nama harus antara 2-50 karakter")
    private String userName;
    
    @JsonProperty("userEmail")
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String userEmail;
    
    @JsonProperty("userPassword")
    @NotBlank(message = "Password wajib diisi")
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String userPassword;
    
    @JsonProperty("userProfile")
    private String userProfile;

    // Constructor kosong
    public UserRegistrationDTO() {}

    // Constructor dengan parameter
    public UserRegistrationDTO(String userName, String userEmail, String userPassword, String userProfile) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userProfile = userProfile;
    }
    
    // Getters and Setters
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

    public String getUserPassword() { 
        return userPassword; 
    }

    public void setUserPassword(String userPassword) { 
        this.userPassword = userPassword; 
    }

    public String getUserProfile() { 
        return userProfile; 
    }

    public void setUserProfile(String userProfile) { 
        this.userProfile = userProfile; 
    }
}
