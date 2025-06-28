package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryFilterRequest {
    
    private String language;
    private String titleContains;
    private LocalDateTime createdAtStart;
    private LocalDateTime createdAtEnd;
    private Integer page;
    private Integer size;
    
    /**
     * Check if any filter is applied
     * @return true if any filter is set
     */
    public boolean hasFilters() {
        return language != null || 
               titleContains != null || 
               createdAtStart != null || 
               createdAtEnd != null;
    }
    
    /**
     * Check if date range filter is complete (both start and end dates)
     * @return true if both start and end dates are provided
     */
    public boolean hasCompleteDateRange() {
        return createdAtStart != null && createdAtEnd != null;
    }
    
    /**
     * Validate the filter request
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (createdAtStart != null && createdAtEnd != null && createdAtStart.isAfter(createdAtEnd)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        if (page != null && page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        
        if (size != null && (size < 1 || size > 100)) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }
} 