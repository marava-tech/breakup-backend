package com.breakupstories.controller;

import com.breakupstories.dto.AbuseAnalysisResult;
import com.breakupstories.service.AbuseDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for manual comment abuse analysis testing
 */
@RestController
@RequestMapping("/api/comment-analysis")
public class CommentAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(CommentAnalysisController.class);

    private final AbuseDetectionService abuseDetectionService;

    public CommentAnalysisController(AbuseDetectionService abuseDetectionService) {
        this.abuseDetectionService = abuseDetectionService;
    }

    /**
     * Manual API to test comment abuse detection
     *
     * @param request Request containing the comment text to analyze
     * @return Analysis result with isAbusive, category, confidence, and explanation
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testCommentAnalysis(@RequestBody Map<String, String> request) {
        try {
            String commentText = request.get("text");

            if (commentText == null || commentText.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Comment text is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Testing comment abuse analysis for text: {}", commentText.substring(0, Math.min(50, commentText.length())));

            // Analyze the comment text
            AbuseAnalysisResult result = abuseDetectionService.analyzeCommentText(commentText);

            // Build response
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("isAbusive", result.isAbusive());
            data.put("category", result.getCategory());
            data.put("confidence", result.getConfidence());
            data.put("explanation", result.getExplanation());
            response.put("success", true);
            response.put("data", data);

            log.info("Comment analysis completed - isAbusive: {}, category: {}, confidence: {}",
                    result.isAbusive(), result.getCategory(), result.getConfidence());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in comment analysis test: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to analyze comment: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint for comment analysis
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Comment Analysis Service");
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
