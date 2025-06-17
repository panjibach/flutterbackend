package com.example.flutterbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String resourceName, Long resourceId, Long userId) {
        super(String.format("User dengan ID %d tidak memiliki akses ke %s dengan ID %d", userId, resourceName, resourceId));
    }
}
