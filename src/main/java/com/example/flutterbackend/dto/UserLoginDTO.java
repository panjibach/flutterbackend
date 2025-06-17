package com.example.flutterbackend.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UserLoginDTO {
    
    @JsonProperty("userEmail")
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String userEmail;
    
    @JsonProperty("userPassword")
    @NotBlank(message = "Password wajib diisi")
    private String userPassword;

    // Constructor kosong
    public UserLoginDTO() {}

    // Constructor dengan parameter
    public UserLoginDTO(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    // Getters and Setters
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
}
