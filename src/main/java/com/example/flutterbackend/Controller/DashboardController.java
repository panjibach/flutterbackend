package com.example.flutterbackend.Controller;

import com.example.flutterbackend.dto.DashboardDTO;
import com.example.flutterbackend.service.DashboardService;
import com.example.flutterbackend.security.AuthenticationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AuthenticationHelper authHelper;

    // Dashboard data untuk user (dipanggil saat login atau refresh)
    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardDTO> getDashboardData(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            System.out.println("=== GET /api/dashboard/user/" + userId + " called ===");
            
            // Validasi akses user
            if (!authHelper.validateUserAccess(userId, request)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Anda tidak memiliki akses ke dashboard user ini");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            
            DashboardDTO dashboard = dashboardService.getDashboardData(userId);
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            System.err.println("Error getting dashboard data: " + e.getMessage());
            throw e;
        }
    }

    // Error handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        System.err.println("Exception caught: " + ex.getMessage());
        ex.printStackTrace();
        
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
