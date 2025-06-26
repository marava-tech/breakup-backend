package com.breakupstories.service;

import com.breakupstories.dto.AudioInfoResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AudioStreamService {
    
    /**
     * Stream audio for a story with support for range requests
     * @param storyId The story ID
     * @param rangeHeader The Range header for partial content requests
     * @return ResponseEntity with the audio resource
     */
    ResponseEntity<Resource> streamAudio(String storyId, String rangeHeader);
    
    /**
     * Get audio information for a story
     * @param storyId The story ID
     * @return AudioInfoResponse with audio details
     */
    AudioInfoResponse getAudioInfo(String storyId);
} 