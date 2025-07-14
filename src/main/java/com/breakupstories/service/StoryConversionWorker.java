package com.breakupstories.service;

import com.breakupstories.model.Story;
import com.breakupstories.model.StoryDataStore;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.breakupstories.repository.StoryRepository;
import com.breakupstories.util.RequestIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.breakupstories.util.TimestampUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Background worker that converts stories with STORY_CONVERSION_PENDING status to final Story entities
 * 
 * Workflow:
 * 1. Fetches stories with STORY_CONVERSION_PENDING status
 * 2. Marks them as COMPLETED immediately after fetching to prevent duplicate processing
 * 3. Converts StoryDataStore to final Story entity
 * 4. Any errors mark the story as FAILED with error details
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoryConversionWorker {
    
    private final StoryDataStoreRepository storyDataStoreRepository;
    private final StoryRepository storyRepository;
    private final StoryStatusService storyStatusService;
    private final DefaultConfigService defaultConfigService;
    private final FirstStoryRewardService firstStoryRewardService;
    
    /**
     * Process conversions every 5 minutes
     */
    @Scheduled(fixedRate = 60000) // 1 minutes
    public void convertStories() {
        String requestId = RequestIdGenerator.generateRequestId();
        log.info("Starting story conversion worker (Request ID: {})", requestId);
        
        try {
            // Fetch stories with STORY_CONVERSION_PENDING status, ordered by creation time (oldest first)
            List<StoryDataStore> pendingStories = storyDataStoreRepository.findByProcessingStatusOrderByCreatedAtAscLimit(StoryDataStore.ProcessingStatus.STORY_CONVERSION_PENDING, 10);
            
            if (pendingStories.isEmpty()) {
                log.info("No stories pending conversion (Request ID: {})", requestId);
                return;
            }
            
            log.info("Found {} stories pending conversion (Request ID: {})", pendingStories.size(), requestId);
            
            // Process each pending story
            for (StoryDataStore dataStore : pendingStories) {
                try {
                    // Mark as COMPLETED immediately after fetching to prevent duplicate processing
                    dataStore.setProcessingStatus(StoryDataStore.ProcessingStatus.COMPLETED);
                    storyDataStoreRepository.save(dataStore);
                    log.info("Marked story {} as COMPLETED before processing (Request ID: {})", dataStore.getId(), requestId);
                    
                    // Now proceed with conversion
                    convertStory(dataStore, requestId);
                    
                } catch (Exception e) {
                    log.error("Error converting story {} (Request ID: {}): {}", dataStore.getId(), requestId, e.getMessage(), e);
                    markStoryAsFailed(dataStore, "Conversion failed: " + e.getMessage());
                }
            }
            
            log.info("Story conversion worker completed (Request ID: {})", requestId);
            
        } catch (Exception e) {
            log.error("Error in story conversion worker (Request ID: {}): {}", requestId, e.getMessage(), e);
        }
    }
    
    /**
     * Convert a single processed story to final Story entity
     */
    private void convertStory(StoryDataStore dataStore, String requestId) {
        log.info("Converting story: {} (Request ID: {})", dataStore.getId(), requestId);
        
        try {
            // Update status to CONVERTING using StoryStatusService
            storyStatusService.updateStatusInBothCollections(dataStore.getStoryId(), Story.StoryStatus.CONVERTING);
            
            // Check if story is valid based on story analysis
            if (dataStore.getStoryAnalysisResponse() != null && 
                dataStore.getStoryAnalysisResponse().getAnalysis() != null &&
                !dataStore.getStoryAnalysisResponse().getAnalysis().isValidStory()) {
                
                log.warn("Story {} marked as invalid by AI analysis - not a love/breakup related story (Request ID: {})", 
                        dataStore.getId(), requestId);
                
                // Create rejected story with proper rejection reason
                createRejectedStoryFromDataStore(dataStore, requestId, 
                    "Story rejected: Not a love or breakup related story. The content does not meet our platform's criteria for relationship-focused narratives.");
                
                // Update status to REJECTED using StoryStatusService
                storyStatusService.updateStatusInBothCollections(dataStore.getStoryId(), Story.StoryStatus.REJECTED);
                
            } else {
                // Story is valid, proceed with normal conversion
                createStoryFromDataStore(dataStore, requestId);
                
                // Update status to COMPLETED using StoryStatusService
                storyStatusService.updateStatusInBothCollections(dataStore.getStoryId(), Story.StoryStatus.ACTIVE);
            }
            
        } catch (Exception e) {
            log.error("Error converting story {} (Request ID: {}): {}", dataStore.getId(), requestId, e.getMessage(), e);
            markStoryAsFailed(dataStore, "Conversion failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Create Story entity from StoryDataStore
     */
    private void createStoryFromDataStore(StoryDataStore dataStore, String requestId) {
        log.info("Creating Story from StoryDataStore for story: {} (Request ID: {})", dataStore.getId(), requestId);
        
        try {
            // Build rejection reasons from all error fields
            StringBuilder rejectionReasons = new StringBuilder();
            
            // Add general errors
            if (dataStore.getErrors() != null && !dataStore.getErrors().isEmpty()) {
                rejectionReasons.append("General errors: ").append(String.join(", ", dataStore.getErrors())).append("; ");
            }
            

            
            // Add step errors from map
            if (dataStore.getStepErrors() != null && !dataStore.getStepErrors().isEmpty()) {
                dataStore.getStepErrors().forEach((step, error) -> 
                    rejectionReasons.append(step).append(" error: ").append(error).append("; "));
            }

            // Determine creation type from metadata
            Story.CreationType creationType = Story.CreationType.UPLOADED; // Default
            if (dataStore.getUploadMetadata() != null && dataStore.getUploadMetadata().get("creationType") != null) {
                String creationTypeStr = dataStore.getUploadMetadata().get("creationType");
                try {
                    creationType = Story.CreationType.valueOf(creationTypeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid creation type in metadata: {}, using default UPLOADED", creationTypeStr);
                }
            }
            
            // Create Story entity
            Story story = Story.builder()
                    .id(dataStore.getStoryId())
                    .userId(dataStore.getUserId())
                    .title(dataStore.getTitle())
                    .contents(extractContentsFromDataStore(dataStore))
                    .tags(extractTagsFromDataStore(dataStore))
                    .emotions(extractEmotionsFromDataStore(dataStore))
                    .language(dataStore.getLanguage())
                    .audioUrl(dataStore.getAudioUrl())
                    .thumbnailUrl(extractThumbnailUrl(dataStore))
                    .storyImages(extractStoryImages(dataStore))
                    .duration(dataStore.getDuration())
                    .rejectionReasons(!rejectionReasons.isEmpty() ? List.of(rejectionReasons.toString()) : null)
                    .status(Story.StoryStatus.ACTIVE) // All converted stories are active
                    .creationType(creationType)
                    .createdAt(dataStore.getCreatedAt())
                    .updatedAt(TimestampUtil.currentLocalDateTime())
                    .build();
            
              storyRepository.save(story);
            
            // Check for first story reward
            boolean rewardGiven = firstStoryRewardService.checkAndRewardFirstStory(dataStore.getUserId(), dataStore.getStoryId());
            if (rewardGiven) {
                log.info("First story reward check completed for story: {} (Request ID: {})", dataStore.getId(), requestId);
            }
            
            log.info("Successfully created Story from StoryDataStore for story: {} (Request ID: {})", dataStore.getId(), requestId);

        } catch (Exception e) {
            log.error("Error creating Story from StoryDataStore for story {} (Request ID: {}): {}", dataStore.getId(), requestId, e.getMessage(), e);
            throw new RuntimeException("Failed to create Story from StoryDataStore: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract contents from data store
     */
    private List<com.breakupstories.model.Content> extractContentsFromDataStore(StoryDataStore dataStore) {
        List<com.breakupstories.model.Content> contents = new ArrayList<>();
        
        // First, try to extract from paragraphRewriteResponse (preferred)
        if (dataStore.getParagraphRewriteResponse() != null && 
            dataStore.getParagraphRewriteResponse().getParagraphs() != null) {
            
            log.info("Extracting {} paragraphs from paragraphRewriteResponse for story: {}", 
                    dataStore.getParagraphRewriteResponse().getParagraphs().size(), dataStore.getId());
            
            for (int i = 0; i < dataStore.getParagraphRewriteResponse().getParagraphs().size(); i++) {
                var paragraphContent = dataStore.getParagraphRewriteResponse().getParagraphs().get(i);
                
                com.breakupstories.model.Content content = com.breakupstories.model.Content.builder()
                        .type(com.breakupstories.model.Content.ContentType.TEXT)
                        .data(paragraphContent.getRewrittenText())
                        .orderIndex(paragraphContent.getParagraphNumber() != null ? paragraphContent.getParagraphNumber() : i + 1)
                        .build();
                
                contents.add(content);
            }
            
            log.info("Successfully extracted {} contents from paragraphRewriteResponse for story: {}", 
                    contents.size(), dataStore.getId());
            
        } else if (dataStore.getStoryRewriteResponse() != null && 
                   dataStore.getStoryRewriteResponse().getRewrittenText() != null) {
            
            // Fallback to storyRewriteResponse if paragraphRewriteResponse is not available
            log.info("Using storyRewriteResponse as fallback for story: {}", dataStore.getId());
            
            com.breakupstories.model.Content content = com.breakupstories.model.Content.builder()
                    .type(com.breakupstories.model.Content.ContentType.TEXT)
                    .data(dataStore.getStoryRewriteResponse().getRewrittenText())
                    .orderIndex(1)
                    .build();
            
            contents.add(content);
            
        } else {
            // Last resort - create a placeholder content
            log.warn("No content available for story: {}, creating placeholder", dataStore.getId());
            
            com.breakupstories.model.Content content = com.breakupstories.model.Content.builder()
                    .type(com.breakupstories.model.Content.ContentType.TEXT)
                    .data("Story content not available")
                    .orderIndex(1)
                    .build();
            
            contents.add(content);
        }
        
        return contents;
    }
    
    /**
     * Extract tags from data store
     */
    private List<String> extractTagsFromDataStore(StoryDataStore dataStore) {
        if (dataStore.getStoryAnalysisResponse() != null && 
            dataStore.getStoryAnalysisResponse().getAnalysis() != null &&
            dataStore.getStoryAnalysisResponse().getAnalysis().getTags() != null) {
            
            List<String> tags = dataStore.getStoryAnalysisResponse().getAnalysis().getTags();
            // Filter out empty strings as an additional safety measure
            List<String> filteredTags = tags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .toList();
            
            log.info("Extracted {} tags from story analysis for story: {} (filtered from {} original tags)", 
                    filteredTags.size(), dataStore.getId(), tags.size());
            return filteredTags;
        }
        
        log.warn("No tags available from story analysis for story: {}", dataStore.getId());
        return new ArrayList<>();
    }
    
    /**
     * Extract thumbnail URL from data store
     */
    private String extractThumbnailUrl(StoryDataStore dataStore) {
        if (dataStore.getImagesResponse() != null && 
            dataStore.getImagesResponse().getThumbnailImageUrl() != null) {
            log.info("Using thumbnail from images response for story: {}", dataStore.getId());
            return dataStore.getImagesResponse().getThumbnailImageUrl();
        }
        
        log.warn("No thumbnail available from images response for story: {}, using default", dataStore.getId());
        return defaultConfigService.getDefaultThumbnailUrl();
    }
    
    /**
     * Extract story images from data store
     */
    private List<String> extractStoryImages(StoryDataStore dataStore) {
        if (dataStore.getImagesResponse() != null && 
            dataStore.getImagesResponse().getStoryImageUrls() != null) {
            log.info("Using {} story images from images response for story: {}", 
                    dataStore.getImagesResponse().getStoryImageUrls().size(), dataStore.getId());
            return dataStore.getImagesResponse().getStoryImageUrls();
        }
        
        log.warn("No story images available from images response for story: {}, using default", dataStore.getId());
        return defaultConfigService.getDefaultStoryImages();
    }
    
    /**
     * Extract emotions from data store
     */
    private List<com.breakupstories.model.Emotion> extractEmotionsFromDataStore(StoryDataStore dataStore) {
        List<com.breakupstories.model.Emotion> emotions = new ArrayList<>();
        
        if (dataStore.getStoryAnalysisResponse() != null && 
            dataStore.getStoryAnalysisResponse().getAnalysis() != null &&
            dataStore.getStoryAnalysisResponse().getAnalysis().getEmotionsWithScores() != null) {
            
            Map<String, Double> emotionScores = dataStore.getStoryAnalysisResponse().getAnalysis().getEmotionsWithScores();
            
            for (Map.Entry<String, Double> entry : emotionScores.entrySet()) {
                try {
                    // Use emotion type as string directly
                    String emotionType = entry.getKey();
                    
                    com.breakupstories.model.Emotion emotion = com.breakupstories.model.Emotion.builder()
                            .type(emotionType)
                            .score(entry.getValue())
                            .build();
                    
                    emotions.add(emotion);
                } catch (Exception e) {
                    log.warn("Error processing emotion type: {} for story: {}", entry.getKey(), dataStore.getId(), e);
                }
            }
            
            log.info("Extracted {} emotions from story analysis for story: {}", emotions.size(), dataStore.getId());
        } else {
            log.warn("No emotions available from story analysis for story: {}", dataStore.getId());
        }
        
        return emotions;
    }
    
    /**
     * Mark story as failed
     */
    private void markStoryAsFailed(StoryDataStore dataStore, String errorMessage) {
        log.error("Marking story as failed: {} - {}", dataStore.getId(), errorMessage);
        
        try {
            // Update the StoryDataStore status to FAILED
            dataStore.setProcessingStatus(StoryDataStore.ProcessingStatus.FAILED);
            dataStore.setErrorMessage(errorMessage);
            storyDataStoreRepository.save(dataStore);
            
            // Use StoryStatusService to mark story as failed in both collections
            storyStatusService.markStoryAsFailed(dataStore.getStoryId(), errorMessage);
            
            log.info("Story marked as failed: {}", dataStore.getId());
            
        } catch (Exception e) {
            log.error("Error marking story as failed: {}", dataStore.getId(), e);
        }
    }
    

    


    /**
     * Create rejected Story entity from StoryDataStore with custom rejection reason
     */
    private Story createRejectedStoryFromDataStore(StoryDataStore dataStore, String requestId, String rejectionReason) {
        log.info("Creating rejected Story from StoryDataStore for story: {} (Request ID: {})", dataStore.getId(), requestId);
        
        try {
            // Build rejection reasons from all error fields
            StringBuilder rejectionReasons = new StringBuilder();
            
            // Add the custom rejection reason first
            rejectionReasons.append(rejectionReason).append("; ");
            
            // Add general errors
            if (dataStore.getErrors() != null && !dataStore.getErrors().isEmpty()) {
                rejectionReasons.append("General errors: ").append(String.join(", ", dataStore.getErrors())).append("; ");
            }
            
            // Add step-specific errors
            // (AI-related error fields removed)
            
            // Create Story entity with REJECTED status
            Story story = Story.builder()
                    .id(dataStore.getStoryId())
                    .userId(dataStore.getUserId())
                    .title(dataStore.getTitle() != null ? dataStore.getTitle() : "Rejected Story")
                    .contents(extractContentsFromDataStore(dataStore))
                    .tags(extractTagsFromDataStore(dataStore))
                    .emotions(extractEmotionsFromDataStore(dataStore))
                    .language(dataStore.getLanguage())
                    .audioUrl(dataStore.getAudioUrl())
                    .thumbnailUrl(extractThumbnailUrl(dataStore))
                    .storyImages(extractStoryImages(dataStore))
                    .duration(dataStore.getDuration())
                    .rejectionReasons(List.of(rejectionReasons.toString()))
                    .status(Story.StoryStatus.REJECTED) // Invalid stories are rejected
                    .createdAt(dataStore.getCreatedAt())
                    .updatedAt(TimestampUtil.currentLocalDateTime())
                    .build();
            
            // Save the Story entity
            Story savedStory = storyRepository.save(story);
            
            log.info("Successfully created rejected Story from StoryDataStore for story: {} (Request ID: {})", dataStore.getId(), requestId);
            
            return savedStory;
            
        } catch (Exception e) {
            log.error("Error creating rejected Story from StoryDataStore for story {} (Request ID: {}): {}", dataStore.getId(), requestId, e.getMessage(), e);
            throw new RuntimeException("Failed to create rejected Story from StoryDataStore: " + e.getMessage(), e);
        }
    }
} 