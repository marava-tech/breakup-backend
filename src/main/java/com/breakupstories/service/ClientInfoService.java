package com.breakupstories.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
public class ClientInfoService {
    
    /**
     * Extract client information from the current HTTP request
     */
    public ClientInfo extractClientInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ClientInfo.builder()
                    .userAgent("Unknown")
                    .ipAddress("Unknown")
                    .sessionId("Unknown")
                    .build();
        }
        
        HttpServletRequest request = attributes.getRequest();
        return extractClientInfo(request);
    }
    
    /**
     * Extract client information from a specific HTTP request
     */
    public ClientInfo extractClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            userAgent = "Unknown";
        }
        
        String ipAddress = getClientIpAddress(request);
        String sessionId = getSessionId(request);
        
        return ClientInfo.builder()
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .sessionId(sessionId)
                .build();
    }
    
    /**
     * Get the real client IP address, handling proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String xClientIp = request.getHeader("X-Client-IP");
        if (xClientIp != null && !xClientIp.isEmpty() && !"unknown".equalsIgnoreCase(xClientIp)) {
            return xClientIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get or generate a session ID
     */
    private String getSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-ID");
        if (sessionId != null && !sessionId.isEmpty()) {
            return sessionId;
        }
        
        // Try to get from session
        if (request.getSession(false) != null) {
            String existingSessionId = (String) request.getSession().getAttribute("sessionId");
            if (existingSessionId != null) {
                return existingSessionId;
            }
        }
        
        // Generate new session ID
        String newSessionId = UUID.randomUUID().toString();
        if (request.getSession(false) != null) {
            request.getSession().setAttribute("sessionId", newSessionId);
        }
        
        return newSessionId;
    }
    
    /**
     * Client information holder
     */
    public static class ClientInfo {
        private final String userAgent;
        private final String ipAddress;
        private final String sessionId;
        
        private ClientInfo(Builder builder) {
            this.userAgent = builder.userAgent;
            this.ipAddress = builder.ipAddress;
            this.sessionId = builder.sessionId;
        }
        
        public String getUserAgent() {
            return userAgent;
        }
        
        public String getIpAddress() {
            return ipAddress;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String userAgent;
            private String ipAddress;
            private String sessionId;
            
            public Builder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }
            
            public Builder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }
            
            public Builder sessionId(String sessionId) {
                this.sessionId = sessionId;
                return this;
            }
            
            public ClientInfo build() {
                return new ClientInfo(this);
            }
        }
    }
} 