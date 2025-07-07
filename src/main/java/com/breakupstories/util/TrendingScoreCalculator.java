package com.breakupstories.util;

/**
 * Utility class for calculating trending scores based on engagement metrics
 */
public class TrendingScoreCalculator {
    
    // Weights for different engagement metrics
    private static final double LIKES_WEIGHT = 1.0;
    private static final double VIEWS_WEIGHT = 0.4;
    private static final double COMMENTS_WEIGHT = 0.6;
    
    /**
     * Calculate trending score based on engagement metrics
     * @param likeCount Number of likes
     * @param viewCount Number of views
     * @param commentCount Number of comments
     * @return Trending score (higher is more trending)
     */
    public static double calculateTrendingScore(long likeCount, long viewCount, long commentCount) {
        return (likeCount * LIKES_WEIGHT) + 
               (viewCount * VIEWS_WEIGHT) + 
               (commentCount * COMMENTS_WEIGHT);
    }
    
    /**
     * Calculate trending score with custom weights
     * @param likeCount Number of likes
     * @param viewCount Number of views
     * @param commentCount Number of comments
     * @param likesWeight Weight for likes
     * @param viewsWeight Weight for views
     * @param commentsWeight Weight for comments
     * @return Trending score (higher is more trending)
     */
    public static double calculateTrendingScore(long likeCount, long viewCount, long commentCount,
                                             double likesWeight, double viewsWeight, double commentsWeight) {
        return (likeCount * likesWeight) + 
               (viewCount * viewsWeight) + 
               (commentCount * commentsWeight);
    }
    
    /**
     * Get the default likes weight
     */
    public static double getLikesWeight() {
        return LIKES_WEIGHT;
    }
    
    /**
     * Get the default views weight
     */
    public static double getViewsWeight() {
        return VIEWS_WEIGHT;
    }
    
    /**
     * Get the default comments weight
     */
    public static double getCommentsWeight() {
        return COMMENTS_WEIGHT;
    }
} 