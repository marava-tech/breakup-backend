package com.breakupstories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for deserializing abuse analysis results from GPT
 */
public class AbuseAnalysisResult {
    @JsonProperty("isAbusive")
    private boolean isAbusive;
    private String category;
    private Double confidence;
    private String explanation;

    // Manual getters and setters for Lombok compatibility
    public boolean isAbusive() { return isAbusive; }
    public void setAbusive(boolean abusive) { isAbusive = abusive; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
