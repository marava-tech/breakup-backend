package com.breakupstories.dto;

import com.breakupstories.model.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold story data with trending score for sorting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryWithTrendingScore {
    private Story story;
    private double trendingScore;
    private long likeCount;
    private long viewCount;
    private long commentCount;
    
    /**
     * Create from Story and engagement metrics
     */
    public static StoryWithTrendingScore fromStory(Story story, long likeCount, long viewCount, long commentCount) {
        double trendingScore = com.breakupstories.util.TrendingScoreCalculator.calculateTrendingScore(
                likeCount, viewCount, commentCount);
        
        return StoryWithTrendingScore.builder()
                .story(story)
                .trendingScore(trendingScore)
                .likeCount(likeCount)
                .viewCount(viewCount)
                .commentCount(commentCount)
                .build();
    }
} 