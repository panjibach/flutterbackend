package com.example.flutterbackend.service;

import org.springframework.stereotype.Service;

import com.example.flutterbackend.model.User;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SessionService {

    private static final String LAST_ACTIVITY_KEY = "LAST_ACTIVITY";
    private static final String USER_ID_KEY = "USER_ID";
    private static final String USER_EMAIL_KEY = "USER_EMAIL";
    private static final String SESSION_DATA_KEY = "SESSION_DATA";

    // Create session with user information
    public void createSession(HttpSession session, User user) {
        try {
            if (session == null || user == null) {
                System.err.println("Session or user is null, cannot create session");
                return;
            }
            
            System.out.println("=== Creating Session for User: " + user.getUserId() + " ===");
            System.out.println("Session ID: " + session.getId());
            
            // Store basic user info - JANGAN simpan objek User lengkap
            session.setAttribute(USER_ID_KEY, user.getUserId());
            session.setAttribute(USER_EMAIL_KEY, user.getUserEmail());
            
            // Update last activity timestamp
            session.setAttribute(LAST_ACTIVITY_KEY, System.currentTimeMillis());
            
            // Create session data map
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("loginTime", System.currentTimeMillis());
            sessionData.put("lastActivity", System.currentTimeMillis());
            sessionData.put("userAgent", "API_CLIENT");
            
            session.setAttribute(SESSION_DATA_KEY, sessionData);
            
            // Set session timeout (1 hour)
            session.setMaxInactiveInterval(3600);
            
            System.out.println("Session created successfully with timeout: " + session.getMaxInactiveInterval() + " seconds");
            
        } catch (Exception e) {
            System.err.println("Error creating session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update session with user information
    public void updateSession(HttpSession session, User user) {
        try {
            if (session == null || user == null) {
                return;
            }
            
            // If session doesn't have user data, create it
            Object existingUserId = session.getAttribute(USER_ID_KEY);
            if (existingUserId == null) {
                createSession(session, user);
                return;
            }
            
            // Update last activity timestamp
            session.setAttribute(LAST_ACTIVITY_KEY, System.currentTimeMillis());
            
            // Update session data
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = (Map<String, Object>) session.getAttribute(SESSION_DATA_KEY);
            if (sessionData != null) {
                sessionData.put("lastActivity", System.currentTimeMillis());
            }
            
        } catch (Exception e) {
            System.err.println("Error updating session: " + e.getMessage());
        }
    }
    
    // Get user ID from session
    public Long getUserIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        try {
            Object userId = session.getAttribute(USER_ID_KEY);
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
        } catch (Exception e) {
            System.err.println("Error getting user ID from session: " + e.getMessage());
        }
        return null;
    }
    
    // Get user email from session
    public String getUserEmailFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        try {
            Object userEmail = session.getAttribute(USER_EMAIL_KEY);
            return userEmail instanceof String ? (String) userEmail : null;
        } catch (Exception e) {
            System.err.println("Error getting user email from session: " + e.getMessage());
            return null;
        }
    }
    
    // Check if session is active
    public boolean isSessionActive(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        try {
            // Check if session has user ID
            Object userId = session.getAttribute(USER_ID_KEY);
            if (userId == null) {
                return false;
            }
            
            // Check last activity timestamp
            Object lastActivity = session.getAttribute(LAST_ACTIVITY_KEY);
            if (lastActivity == null) {
                return false;
            }
            
            // Session is active if last activity is within the last hour
            if (lastActivity instanceof Long) {
                long lastActivityTime = (Long) lastActivity;
                return (System.currentTimeMillis() - lastActivityTime) < 3600000; // 1 hour
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error checking session activity: " + e.getMessage());
            return false;
        }
    }
    
    // Update last activity timestamp
    public void updateLastActivity(HttpSession session) {
        if (session != null) {
            try {
                session.setAttribute(LAST_ACTIVITY_KEY, System.currentTimeMillis());
                
                @SuppressWarnings("unchecked")
                Map<String, Object> sessionData = (Map<String, Object>) session.getAttribute(SESSION_DATA_KEY);
                if (sessionData != null) {
                    sessionData.put("lastActivity", System.currentTimeMillis());
                }
            } catch (Exception e) {
                System.err.println("Error updating last activity: " + e.getMessage());
            }
        }
    }
    
    // Store data in session
    public void storeInSession(HttpSession session, String key, Object value) {
        if (session != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> sessionData = (Map<String, Object>) session.getAttribute(SESSION_DATA_KEY);
                if (sessionData == null) {
                    sessionData = new HashMap<>();
                    session.setAttribute(SESSION_DATA_KEY, sessionData);
                }
                sessionData.put(key, value);
            } catch (Exception e) {
                System.err.println("Error storing data in session: " + e.getMessage());
            }
        }
    }
    
    // Get data from session
    public Object getFromSession(HttpSession session, String key) {
        if (session == null) {
            return null;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionData = (Map<String, Object>) session.getAttribute(SESSION_DATA_KEY);
            if (sessionData != null) {
                return sessionData.get(key);
            }
        } catch (Exception e) {
            System.err.println("Error getting data from session: " + e.getMessage());
        }
        return null;
    }
    
    // Invalidate session
    public void invalidateSession(HttpSession session) {
        if (session != null) {
            try {
                System.out.println("Invalidating session: " + session.getId());
                session.invalidate();
                System.out.println("Session invalidated successfully");
            } catch (IllegalStateException e) {
                System.out.println("Session already invalidated");
            } catch (Exception e) {
                System.err.println("Error invalidating session: " + e.getMessage());
            }
        }
    }
}
