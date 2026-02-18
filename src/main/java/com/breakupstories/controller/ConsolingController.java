package com.breakupstories.controller;

import com.breakupstories.model.StoryDataStore;
import com.breakupstories.model.User;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.breakupstories.repository.UserRepository;
import com.breakupstories.service.ConsolingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for consoling message generation
 */
@RestController
@RequestMapping("/api/story")
public class ConsolingController {

    private static final Logger log = LoggerFactory.getLogger(ConsolingController.class);

    private final StoryDataStoreRepository storyDataStoreRepository;
    private final UserRepository userRepository;
    private final ConsolingService consolingService;

    public ConsolingController(StoryDataStoreRepository storyDataStoreRepository, UserRepository userRepository,
            ConsolingService consolingService) {
        this.storyDataStoreRepository = storyDataStoreRepository;
        this.userRepository = userRepository;
        this.consolingService = consolingService;
    }

    /**
     * Generate consoling message for a story
     *
     * @param storyId The ID of the story
     * @param persona The persona to use for message generation
     * @return Consoling message response
     */
    @GetMapping("/{storyId}/consoling-message")
    public ResponseEntity<Map<String, Object>> generateConsolingMessage(
            @PathVariable String storyId,
            @RequestParam String persona) {

        try {
            log.info("Generating consoling message for story: {} with persona: {}", storyId, persona);

            // Fetch story
            Optional<StoryDataStore> storyOpt = storyDataStoreRepository.findByStoryId(storyId);

            if (storyOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Story not found with ID: " + storyId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            StoryDataStore story = storyOpt.get();

            // Check if story has rewritten content
            if (story.getStoryRewriteResponse() == null ||
                    story.getStoryRewriteResponse().getRewrittenText() == null ||
                    story.getStoryRewriteResponse().getRewrittenText().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Story does not have rewritten content");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Fetch user
            User user = userRepository.findById(story.getUserId())
                    .orElse(null);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found for story: " + storyId);
                return ResponseEntity.internalServerError().body(errorResponse);
            }

            // Generate consoling message
            var consolingResponse = consolingService.generateConsolingMessage(storyId, persona);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", consolingResponse);

            log.info("Successfully generated consoling message for story: {}", storyId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating consoling message for story: {}", storyId, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to generate consoling message: " + e.getMessage());
            errorResponse.put("storyId", storyId);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
