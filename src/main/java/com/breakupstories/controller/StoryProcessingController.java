package com.breakupstories.controller;

import com.breakupstories.model.StoryDataStore;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.breakupstories.service.StoryProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for triggering story processing workflow
 */
@RestController
@RequestMapping("/api/story")
public class StoryProcessingController {

    private static final Logger log = LoggerFactory.getLogger(StoryProcessingController.class);
    private final StoryDataStoreRepository storyDataStoreRepository;
    private final StoryProcessingService storyProcessingService;

    public StoryProcessingController(StoryDataStoreRepository storyDataStoreRepository,
            StoryProcessingService storyProcessingService) {
        this.storyDataStoreRepository = storyDataStoreRepository;
        this.storyProcessingService = storyProcessingService;
    }

    /**
     * Trigger story processing workflow by story ID
     *
     * @param storyId The ID of the story to process
     * @return Processing status response
     */
    @PostMapping("/{storyId}/process")
    public ResponseEntity<Map<String, Object>> triggerStoryProcessing(@PathVariable String storyId) {

        try {
            log.info("Triggering story processing for story ID: {}", storyId);

            // Find story by storyId
            Optional<StoryDataStore> storyOpt = storyDataStoreRepository.findByStoryId(storyId);

            if (storyOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Story not found with ID: " + storyId);
                errorResponse.put("storyId", storyId);
                return ResponseEntity.notFound().build();
            }

            StoryDataStore story = storyOpt.get();

            // Set status to PROCESSING and save immediately before starting processing
            story.setProcessingStatus(StoryDataStore.ProcessingStatus.PROCESSING);
            story.setProcessingStartedAt(java.time.LocalDateTime.now());
            storyDataStoreRepository.save(story);
            log.info("Story {} - Status updated to PROCESSING in controller before starting workflow", storyId);

            // Trigger the actual story processing workflow
            storyProcessingService.processStory(story);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Story processing workflow completed successfully");
            response.put("storyId", storyId);
            response.put("status", "COMPLETED");
            response.put("workflow", "Story processing workflow has been completed");

            log.info("Successfully triggered story processing workflow for story ID: {}", storyId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error triggering story processing for story ID: {}", storyId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to trigger story processing: " + e.getMessage());
            errorResponse.put("storyId", storyId);
            errorResponse.put("error", e.getClass().getSimpleName());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get story processing status by story ID
     *
     * @param storyId The ID of the story
     * @return Story processing status
     */
    @GetMapping("/{storyId}/status")
    public ResponseEntity<Map<String, Object>> getStoryProcessingStatus(@PathVariable String storyId) {

        try {
            log.info("Getting processing status for story ID: {}", storyId);

            // Find story by storyId
            Optional<StoryDataStore> storyOpt = storyDataStoreRepository.findByStoryId(storyId);

            if (storyOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Story not found with ID: " + storyId);
                errorResponse.put("storyId", storyId);
                return ResponseEntity.notFound().build();
            }

            StoryDataStore story = storyOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("storyId", storyId);
            response.put("status", story.getProcessingStatus());
            response.put("hasErrors", story.getErrors() != null && !story.getErrors().isEmpty());
            response.put("errorMessage", story.getErrorMessage());

            // Add processing timestamps if available
            if (story.getProcessingStartedAt() != null) {
                response.put("processingStartedAt", story.getProcessingStartedAt());
            }
            if (story.getProcessingCompletedAt() != null) {
                response.put("processingCompletedAt", story.getProcessingCompletedAt());
            }

            log.info("Successfully retrieved processing status for story ID: {}", storyId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting processing status for story ID: {}", storyId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get processing status: " + e.getMessage());
            errorResponse.put("storyId", storyId);
            errorResponse.put("error", e.getClass().getSimpleName());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
