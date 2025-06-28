package com.breakupstories.controller;

import com.breakupstories.service.CommentAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comment-analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment Analysis", description = "Comment analysis and moderation APIs")
public class CommentAnalysisController {
    
    private final CommentAnalysisService commentAnalysisService;
    
    @PostMapping("/analyze/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Manually analyze a specific comment", description = "Analyze a comment using AI and flag if negative/hateful")
    public ResponseEntity<Map<String, Object>> analyzeComment(@PathVariable String commentId) {
        log.info("Manual comment analysis requested for comment: {}", commentId);
        
        try {
            boolean isPositive = commentAnalysisService.analyzeComment(commentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("commentId", commentId);
            response.put("isPositive", isPositive);
            response.put("status", isPositive ? "APPROVED" : "FLAGGED");
            response.put("message", isPositive ? "Comment is positive" : "Comment flagged as negative/hateful");
            
            log.info("Comment analysis completed for {}: {}", commentId, isPositive ? "POSITIVE" : "NEGATIVE");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing comment {}: {}", commentId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("commentId", commentId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "ERROR");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get comment analysis statistics", description = "Get statistics about recent comment analysis")
    public ResponseEntity<CommentAnalysisService.CommentAnalysisStats> getAnalysisStats() {
        log.info("Comment analysis statistics requested");
        
        try {
            CommentAnalysisService.CommentAnalysisStats stats = commentAnalysisService.getAnalysisStats();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting analysis stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/trigger-analysis")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Trigger manual comment analysis", description = "Manually trigger the comment analysis for recent comments")
    public ResponseEntity<Map<String, Object>> triggerAnalysis() {
        log.info("Manual comment analysis trigger requested");
        
        try {
            // Trigger the scheduled method manually
            commentAnalysisService.analyzeRecentComments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Comment analysis triggered successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error triggering comment analysis: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 