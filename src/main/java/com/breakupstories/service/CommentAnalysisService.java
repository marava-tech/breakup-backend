package com.breakupstories.service;

import com.breakupstories.model.Comment;
import com.breakupstories.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for analyzing comments using AI to detect hateful/negative content
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAnalysisService {
    
    private final CommentRepository commentRepository;
    private final MockAIService mockAIService;
    
    /**
     * Scheduled task to analyze comments every 10 minutes
     * Fetches comments from the last 10 minutes and analyzes them
     */
    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    public void analyzeRecentComments() {
        log.info("Starting scheduled comment analysis...");
        
        try {
            // Calculate time 10 minutes ago
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            
            // Fetch comments from the last 10 minutes
            List<Comment> recentComments = commentRepository.findByCreatedAtAfter(tenMinutesAgo);
            
            log.info("Found {} comments to analyze from the last 10 minutes", recentComments.size());
            
            if (recentComments.isEmpty()) {
                log.info("No comments found in the last 10 minutes");
                return;
            }
            
            int processedCount = 0;
            int flaggedCount = 0;
            
            // Analyze each comment
            for (Comment comment : recentComments) {
                try {
                    boolean isPositive = mockAIService.analyzeComment(comment.getText());
                    
                    if (!isPositive) {
                        // Mark comment as inactive if it's negative/hateful
                        comment.setActive(false);
                        commentRepository.save(comment);
                        flaggedCount++;
                        
                        log.warn("Flagged negative comment - ID: {}, Text: '{}'", 
                                comment.getId(), 
                                comment.getText().substring(0, Math.min(50, comment.getText().length())));
                    }
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    log.error("Error analyzing comment {}: {}", comment.getId(), e.getMessage(), e);
                    // Continue with next comment even if one fails
                }
            }
            
            log.info("Comment analysis completed - Processed: {}, Flagged: {}", processedCount, flaggedCount);
            
        } catch (Exception e) {
            log.error("Error in scheduled comment analysis: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manually analyze a specific comment
     * @param commentId The ID of the comment to analyze
     * @return true if comment is positive, false if negative
     */
    public boolean analyzeComment(String commentId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
            
            boolean isPositive = mockAIService.analyzeComment(comment.getText());
            
            if (!isPositive) {
                comment.setActive(false);
                commentRepository.save(comment);
                log.warn("Manually flagged negative comment - ID: {}, Text: '{}'", 
                        comment.getId(), 
                        comment.getText().substring(0, Math.min(50, comment.getText().length())));
            }
            
            return isPositive;
            
        } catch (Exception e) {
            log.error("Error analyzing comment {}: {}", commentId, e.getMessage(), e);
            throw new RuntimeException("Failed to analyze comment", e);
        }
    }
    
    /**
     * Get statistics about comment analysis
     * @return Analysis statistics
     */
    public CommentAnalysisStats getAnalysisStats() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Comment> recentComments = commentRepository.findByCreatedAtAfter(tenMinutesAgo);
        
        long totalComments = recentComments.size();
        long activeComments = recentComments.stream().filter(Comment::isActive).count();
        long inactiveComments = totalComments - activeComments;
        
        return CommentAnalysisStats.builder()
                .totalComments(totalComments)
                .activeComments(activeComments)
                .inactiveComments(inactiveComments)
                .analysisTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Statistics class for comment analysis
     */
    public static class CommentAnalysisStats {
        private long totalComments;
        private long activeComments;
        private long inactiveComments;
        private LocalDateTime analysisTime;
        
        // Builder pattern
        public static CommentAnalysisStatsBuilder builder() {
            return new CommentAnalysisStatsBuilder();
        }
        
        public static class CommentAnalysisStatsBuilder {
            private CommentAnalysisStats stats = new CommentAnalysisStats();
            
            public CommentAnalysisStatsBuilder totalComments(long totalComments) {
                stats.totalComments = totalComments;
                return this;
            }
            
            public CommentAnalysisStatsBuilder activeComments(long activeComments) {
                stats.activeComments = activeComments;
                return this;
            }
            
            public CommentAnalysisStatsBuilder inactiveComments(long inactiveComments) {
                stats.inactiveComments = inactiveComments;
                return this;
            }
            
            public CommentAnalysisStatsBuilder analysisTime(LocalDateTime analysisTime) {
                stats.analysisTime = analysisTime;
                return this;
            }
            
            public CommentAnalysisStats build() {
                return stats;
            }
        }
        
        // Getters
        public long getTotalComments() { return totalComments; }
        public long getActiveComments() { return activeComments; }
        public long getInactiveComments() { return inactiveComments; }
        public LocalDateTime getAnalysisTime() { return analysisTime; }
    }
} 