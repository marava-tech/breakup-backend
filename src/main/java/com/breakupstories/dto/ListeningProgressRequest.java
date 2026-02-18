package com.breakupstories.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningProgressRequest {

    @NotNull
    private String storyId;

    @NotNull
    private Double progressSeconds;

    // Optional, can be calculated or passed by client
    private Boolean completed;

    // Total duration to calculate completion % if needed
    private Double totalDurationSeconds;
}
