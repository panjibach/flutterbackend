package com.example.flutterbackend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Endpoint untuk melihat semua session aktif
    @GetMapping("/active")
    public ResponseEntity<?> getActiveSessions() {
        try {
            String sql = "SELECT PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, " +
                         "EXPIRY_TIME, PRINCIPAL_NAME FROM SPRING_SESSION WHERE EXPIRY_TIME > ?";
            
            List<Map<String, Object>> sessions = jdbcTemplate.queryForList(sql, System.currentTimeMillis());
            
            // Format data untuk response
            for (Map<String, Object> session : sessions) {
                // Convert timestamps to readable dates
                long creationTime = (Long) session.get("CREATION_TIME");
                long lastAccessTime = (Long) session.get("LAST_ACCESS_TIME");
                long expiryTime = (Long) session.get("EXPIRY_TIME");
                
                session.put("CREATION_TIME_FORMATTED", new Date(creationTime));
                session.put("LAST_ACCESS_TIME_FORMATTED", new Date(lastAccessTime));
                session.put("EXPIRY_TIME_FORMATTED", new Date(expiryTime));
                
                // Get session attributes
                String primaryId = (String) session.get("PRIMARY_ID");
                List<Map<String, Object>> attributes = jdbcTemplate.queryForList(
                    "SELECT ATTRIBUTE_NAME FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID = ?", 
                    primaryId
                );
                session.put("ATTRIBUTES", attributes);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("activeSessions", sessions);
            response.put("count", sessions.size());
            response.put("currentTime", new Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error getting active sessions: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Endpoint untuk melihat detail session saat ini
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Map<String, Object> response = new HashMap<>();
            
            if (session != null) {
                response.put("sessionExists", true);
                response.put("sessionId", session.getId());
                response.put("creationTime", new Date(session.getCreationTime()));
                response.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
                response.put("maxInactiveInterval", session.getMaxInactiveInterval());
                
                // Get attributes
                Map<String, Object> attributes = new HashMap<>();
                for (String name : java.util.Collections.list(session.getAttributeNames())) {
                    Object value = session.getAttribute(name);
                    attributes.put(name, value != null ? value.toString() : "null");
                }
                response.put("attributes", attributes);
                
                // Get database details
                try {
                    Map<String, Object> dbSession = jdbcTemplate.queryForMap(
                        "SELECT * FROM SPRING_SESSION WHERE SESSION_ID = ?", 
                        session.getId()
                    );
                    response.put("databaseRecord", dbSession);
                } catch (Exception e) {
                    response.put("databaseError", e.getMessage());
                }
            } else {
                response.put("sessionExists", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error getting current session: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
