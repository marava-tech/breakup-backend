package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosRealityCheckResponse {
    private boolean success;
    private String realityCheck;
    private String predictedExResponse;
    private String psychologicalExplanation;
    private String recommendedAction;
    private String streakPreservedQuote;
}
