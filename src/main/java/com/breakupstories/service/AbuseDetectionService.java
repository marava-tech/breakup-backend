package com.breakupstories.service;

import com.breakupstories.dto.AbuseAnalysisResult;
import com.breakupstories.dto.OpenAIRequest;
import com.breakupstories.dto.OpenAIResponse;
import com.breakupstories.config.OpenAIConfig;
import com.breakupstories.model.Comment;
import com.breakupstories.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for detecting abuse in comments using GPT API
 */
@Service
public class AbuseDetectionService {
    
    private static final Logger log = LoggerFactory.getLogger(AbuseDetectionService.class);
    
    private final CommentRepository commentRepository;
    private final ObjectMapper objectMapper;
    private final PromptConfigurationService promptConfig;
    
    @Autowired
    private RestTemplate openaiRestTemplate;
    
    @Autowired
    private OpenAIConfig openAIConfig;
    
    public AbuseDetectionService(CommentRepository commentRepository, PromptConfigurationService promptConfig) {
        this.commentRepository = commentRepository;
        this.objectMapper = new ObjectMapper();
        this.promptConfig = promptConfig;
    }
    
    /**
     * Analyze recent comments for abuse detection
     */
    public void analyzeRecentComments() {
        LocalDateTime eightMinutesAgo = LocalDateTime.now().minusMinutes(8);
        List<Comment> comments = commentRepository.findByIsAbusiveFalseAndExplanationIsNullAndCategoryIsNullAndCreatedAtAfter(eightMinutesAgo);
        
        log.info("Found {} comments to analyze for abuse", comments.size());
        
        for (Comment comment : comments) {
            try {
                analyzeAndUpdateComment(comment);
            } catch (Exception e) {
                log.error("Failed to analyze comment {}: {}", comment.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Analyze a single comment for abuse detection
     */
    public AbuseAnalysisResult analyzeCommentText(String commentText) {
        log.info("Analyzing comment text for abuse: {}", commentText.substring(0, Math.min(50, commentText.length())));
        
        try {
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("abuse_detection_system");
            
            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("commentText", commentText);
            String userPrompt = promptConfig.formatPrompt("abuse_detection_user", params);
            
            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIConfig.getModel())
                    .messages(List.of(
                            OpenAIRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenAIRequest.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()
                    ))
                    .maxTokens(300)
                    .temperature(0.0)
                    .build();
            
            // Call OpenAI API
            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/chat/completions",
                    request,
                    OpenAIResponse.class
            );
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                
                // Clean up GPT response if it comes with markdown
                String cleaned = content.trim();
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substring(7).trim();
                }
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substring(3).trim();
                }
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
                }
                
                log.info("GPT Response: {}", cleaned);
                
                // Parse the JSON response
                AbuseAnalysisResult result = objectMapper.readValue(cleaned, AbuseAnalysisResult.class);
                
                log.info("Analysis result - isAbusive: {}, category: {}, confidence: {}", 
                        result.isAbusive(), result.getCategory(), result.getConfidence());
                
                return result;
                
            } else {
                log.error("Invalid response from OpenAI API");
                throw new RuntimeException("Invalid response from OpenAI API");
            }
            
        } catch (Exception e) {
            log.error("Error analyzing comment text: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze comment text: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analyze and update a comment with abuse detection results
     */
    public void analyzeAndUpdateComment(Comment comment) {
        log.info("Analyzing comment {} for abuse detection", comment.getId());
        
        try {
            AbuseAnalysisResult result = analyzeCommentText(comment.getText());
            
            // Update comment with analysis results
            comment.setAbusive(result.isAbusive());
            comment.setCategory(result.getCategory());
            comment.setConfidence(result.getConfidence());
            comment.setExplanation(result.getExplanation());
            
            // Save the updated comment
            commentRepository.save(comment);
            
            log.info("Successfully analyzed and updated comment {} - isAbusive: {}, category: {}", 
                    comment.getId(), result.isAbusive(), result.getCategory());
            
        } catch (Exception e) {
            log.error("Failed to analyze comment {}: {}", comment.getId(), e.getMessage(), e);
            // Mark as failed analysis
            comment.setExplanation("Abuse analysis failed: " + e.getMessage());
            commentRepository.save(comment);
            throw e;
        }
    }
} 