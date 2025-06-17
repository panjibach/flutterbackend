package com.example.flutterbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s dengan %s '%s' sudah ada", resourceName, fieldName, fieldValue));
    }
}
