package com.breakupstories.service;

import com.breakupstories.model.StoryDataStore;
import com.breakupstories.util.CloudinaryUtil;
import com.breakupstories.util.DallEUtil;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for generating thumbnail images for stories
 */
@Service
public class ThumbnailGenerationService {
    
    private static final Logger log = LoggerFactory.getLogger(ThumbnailGenerationService.class);
    
    private final CloudinaryUtil cloudinaryUtil;
    private final DallEUtil dallEUtil;
    private final PromptConfigurationService promptConfig;
    
    public ThumbnailGenerationService(CloudinaryUtil cloudinaryUtil, DallEUtil dallEUtil, PromptConfigurationService promptConfig) {
        this.cloudinaryUtil = cloudinaryUtil;
        this.dallEUtil = dallEUtil;
        this.promptConfig = promptConfig;
    }
    
    /**
     * Generate thumbnail image for the entire story
     * 
     * @param story The story to generate thumbnail for
     * @return true if thumbnail was generated successfully, false otherwise
     */
    public boolean generateThumbnail(StoryDataStore story) {
        log.info("Generating thumbnail for story: {}", story.getStoryId());
        
        try {
            // Get the story text (either from rewrite response or search text)
            String storyText = null;
            if (story.getStoryRewriteResponse() != null && story.getStoryRewriteResponse().getRewrittenText() != null) {
                storyText = story.getStoryRewriteResponse().getRewrittenText();
            } else if (story.getSearchText() != null && !story.getSearchText().trim().isEmpty()) {
                storyText = story.getSearchText().trim();
            } else {
                log.warn("No story text available for thumbnail generation for story: {}", story.getStoryId());
                return false;
            }
            
            if (storyText == null || storyText.trim().isEmpty()) {
                log.warn("Story text is empty for thumbnail generation for story: {}", story.getStoryId());
                return false;
            }
            
            // Generate image prompt for the entire story
            String imagePrompt = generateThumbnailPrompt(storyText);
            
            // Generate image using OpenAI DALL-E
            byte[] imageData = dallEUtil.generateImage(imagePrompt);
            
            // Upload to Cloudinary
            String thumbnailUrl = cloudinaryUtil.uploadImage(imageData, "thumbnail_" + story.getStoryId());
            
            // Save thumbnail URL to ImagesResponse
            if (story.getImagesResponse() == null) {
                story.setImagesResponse(new com.breakupstories.dto.ImagesResponse());
            }
            story.getImagesResponse().setThumbnailImageUrl(thumbnailUrl);
            
            log.info("Successfully generated thumbnail for story: {}, URL: {}", story.getStoryId(), thumbnailUrl);
            return true;
            
        } catch (Exception e) {
            log.warn("Failed to generate thumbnail for story: {} - Error: {}. Continuing without thumbnail.", 
                    story.getStoryId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate image prompt for thumbnail
     */
    private String generateThumbnailPrompt(String rewrittenStory) {
        // Truncate story if too long for prompt
        String truncatedStory = rewrittenStory.length() > 1000 
            ? rewrittenStory.substring(0, 1000) + "..." 
            : rewrittenStory;
        
        // Prepare parameters for thumbnail generation prompt
        Map<String, String> params = new HashMap<>();
        params.put("truncatedStory", truncatedStory);
        
        return promptConfig.formatPrompt("thumbnail_generation", params);
    }
    

} 