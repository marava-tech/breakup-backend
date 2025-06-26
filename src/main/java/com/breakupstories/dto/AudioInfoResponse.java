package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioInfoResponse {
    private String storyId;
    private String audioUrl;
    private long contentLength;
    private MediaType contentType;
    private boolean supportsRangeRequests;
} 