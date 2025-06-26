package com.breakupstories.dto;

import com.breakupstories.model.Audit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Entity type is required")
    private Audit.EntityType entityType;
    
    @NotNull(message = "Action type is required")
    private Audit.ActionType actionType;
    
    @NotBlank(message = "Entity ID is required")
    private String entityId;
    
    // Additional metadata fields
    private String userAgent;
    private String ipAddress;
    private String sessionId;
    private Map<String, Object> metadata;
} 