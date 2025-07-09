package com.breakupstories.service;

import com.breakupstories.dto.AbuseDetectionResponse;
import com.breakupstories.dto.ConsolingMessageResponse;
import com.breakupstories.dto.ParagraphRewriteResponse;
import com.breakupstories.dto.StoryAnalysisResponse;
import com.breakupstories.dto.TranscriptionResponse;
import com.breakupstories.exception.AIServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper service that adds retry logic and 429 error handling to AI service calls
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryableAIService {
    
    private final AIService aiService;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 30;
    
    /**
     * Execute AI service call with retry logic and 429 handling
     * @param operationName Name of the operation for logging
     * @param operation Supplier that performs the AI service call
     * @param <T> Return type of the operation
     * @return Result of the AI service call
     * @throws AIServiceException if all retries fail
     */
    private <T> T executeWithRetry(String operationName, AIServiceOperation<T> operation) {
        int attempt = 1;
        
        while (attempt <= MAX_RETRIES) {
            try {
                log.info("Attempting {} - Attempt {}/{}", operationName, attempt, MAX_RETRIES);
                T result = operation.execute();
                log.info("{} completed successfully on attempt {}", operationName, attempt);
                return result;
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    // 429 error - AI service is busy
                    log.warn("AI service returned 429 (Too Many Requests) for {} on attempt {}/{}. Skipping this story.", 
                            operationName, attempt, MAX_RETRIES);
                    
                    if (attempt < MAX_RETRIES) {
                        log.info("Sleeping for {} seconds before retry...", RETRY_DELAY_SECONDS);
                        try {
                            TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new AIServiceException(operationName, "RETRY_INTERRUPTED", 
                                    "Retry was interrupted: " + ie.getMessage(), ie);
                        }
                    } else {
                        log.error("{} failed after {} attempts due to 429 errors. Skipping this story.", 
                                operationName, MAX_RETRIES);
                        throw new AIServiceException(operationName, "RATE_LIMIT_EXCEEDED", 
                                "AI service is busy after " + MAX_RETRIES + " attempts");
                    }
                } else {
                    // Other HTTP errors - don't retry
                    log.error("{} failed with HTTP error {} on attempt {}", 
                            operationName, e.getStatusCode(), attempt);
                    throw new AIServiceException(operationName, "HTTP_ERROR", 
                            "HTTP error: " + e.getStatusCode() + " - " + e.getMessage(), e);
                }
                
            } catch (RestClientException e) {
                // Network/connection errors - retry
                log.warn("{} failed with network error on attempt {}/{}: {}", 
                        operationName, attempt, MAX_RETRIES, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    log.info("Sleeping for {} seconds before retry...", RETRY_DELAY_SECONDS);
                    try {
                        TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AIServiceException(operationName, "RETRY_INTERRUPTED", 
                                "Retry was interrupted: " + ie.getMessage(), ie);
                    }
                } else {
                    log.error("{} failed after {} attempts due to network errors", operationName, MAX_RETRIES);
                    throw new AIServiceException(operationName, "NETWORK_ERROR", 
                            "Network error after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
                
            } catch (AIServiceException e) {
                // AI service specific errors - don't retry
                log.error("{} failed with AI service error on attempt {}: {}", 
                        operationName, attempt, e.getMessage());
                throw e;
                
            } catch (Exception e) {
                // Other unexpected errors - retry
                log.warn("{} failed with unexpected error on attempt {}/{}: {}", 
                        operationName, attempt, MAX_RETRIES, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    log.info("Sleeping for {} seconds before retry...", RETRY_DELAY_SECONDS);
                    try {
                        TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AIServiceException(operationName, "RETRY_INTERRUPTED", 
                                "Retry was interrupted: " + ie.getMessage(), ie);
                    }
                } else {
                    log.error("{} failed after {} attempts due to unexpected errors", operationName, MAX_RETRIES);
                    throw new AIServiceException(operationName, "UNEXPECTED_ERROR", 
                            "Unexpected error after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
            }
            
            attempt++;
        }
        
        throw new AIServiceException(operationName, "MAX_RETRIES_EXCEEDED", 
                "Operation failed after " + MAX_RETRIES + " attempts");
    }
    
    /**
     * Transcribe audio with retry logic
     */
    public TranscriptionResponse transcribeAudio(String audioUrl, String language) {
        return executeWithRetry("Transcription", () -> aiService.transcribeAudio(audioUrl, language));
    }
    
    /**
     * Rewrite story with retry logic
     */
    public String rewriteStory(String transcript, String language) {
        return executeWithRetry("Story Rewrite", () -> aiService.rewriteStory(transcript, language));
    }
    
    /**
     * Rewrite story into paragraphs with retry logic
     */
    public ParagraphRewriteResponse rewriteStoryIntoParagraphs(String transcript, String language) {
        return executeWithRetry("Paragraph Rewrite", () -> aiService.rewriteStoryIntoParagraphs(transcript, language));
    }
    
    /**
     * Analyze story with retry logic
     */
    public StoryAnalysisResponse analyzeStory(String story, String language) {
        return executeWithRetry("Story Analysis", () -> aiService.analyzeStory(story, language));
    }
    
    /**
     * Detect abuse with retry logic
     */
    public AbuseDetectionResponse detectAbuse(String comment, String language) {
        return executeWithRetry("Abuse Detection", () -> aiService.detectAbuse(comment, language));
    }
    
    /**
     * Generate consoling message with retry logic
     */
    public ConsolingMessageResponse generateConsolingMessage(String story, String language, String gender, Integer age, String consoleBy) {
        return executeWithRetry("Consoling Message", () -> aiService.generateConsolingMessage(story, language, gender, age, consoleBy));
    }
    
    /**
     * Generate animated images with retry logic
     */
    public List<String> generateAnimatedImages(String detailedStory) {
        return executeWithRetry("Image Generation", () -> aiService.generateAnimatedImages(detailedStory));
    }
    
    /**
     * Functional interface for AI service operations
     */
    @FunctionalInterface
    private interface AIServiceOperation<T> {
        T execute() throws Exception;
    }
} 