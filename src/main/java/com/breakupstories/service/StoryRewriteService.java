package com.breakupstories.service;

import com.breakupstories.dto.StoryRewriteResponse;
import com.breakupstories.dto.ParagraphRewriteResponse;
import com.breakupstories.dto.StoryAnalysisResponse;
import com.breakupstories.dto.VisualPromptResponse;
import com.breakupstories.dto.OpenAIRequest;
import com.breakupstories.dto.OpenAIResponse;
import com.breakupstories.dto.ImageUploadResponse;
import com.breakupstories.config.OpenAIConfig;
import com.breakupstories.model.StoryDataStore;
import com.breakupstories.model.Content;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

@Service
public class StoryRewriteService {

    private static final Logger log = LoggerFactory.getLogger(StoryRewriteService.class);

    private final StoryDataStoreRepository storyDataStoreRepository;
    private final OpenAIConfig openAIConfig;
    private final ImageUploadService imageUploadService;
    private final PromptConfigurationService promptConfig;

    @Autowired
    private RestTemplate openaiRestTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StoryRewriteService(StoryDataStoreRepository storyDataStoreRepository,
            OpenAIConfig openAIConfig,
            ImageUploadService imageUploadService,
            PromptConfigurationService promptConfig) {
        this.storyDataStoreRepository = storyDataStoreRepository;
        this.openAIConfig = openAIConfig;
        this.imageUploadService = imageUploadService;
        this.promptConfig = promptConfig;
    }

    /**
     * Process story rewrite
     */
    public void processStoryRewrite(StoryDataStore story) {
        String storyId = story.getStoryId();
        try {
            log.info("Story {} - Step 1: Starting story rewrite processing", storyId);

            // Step 1: Validate transcription response
            log.info("Story {} - Step 1.1: Validating transcription response", storyId);
            if (story.getTranscriptionResponse() == null) {
                throw new RuntimeException("Transcription response is null. Transcription may have failed.");
            }
            log.info("Story {} - Step 1.1: Transcription validation successful", storyId);

            // Step 2: Call story rewrite API
            log.info("Story {} - Step 2: Calling story rewrite API", storyId);
            StoryRewriteResponse rewriteResponse = callStoryRewriteApi(
                    story.getTranscriptionResponse().getTranscription());
            log.info("Story {} - Step 2: Story rewrite API call successful", storyId);

            // Step 3: Update story with rewrite response
            log.info("Story {} - Step 3: Updating story with rewrite response", storyId);
            story.setStoryRewriteResponse(rewriteResponse);
            story.setRewriteCompletedAt(LocalDateTime.now());
            storyDataStoreRepository.save(story);
            log.info("Story {} - Step 3: Story update successful", storyId);

            log.info("Story {} - SUCCESS: Story rewrite processing completed successfully", storyId);

        } catch (Exception e) {
            log.error("Story {} - ERROR: Story rewrite processing failed: {}", storyId, e.getMessage(), e);
            story.setRewriteError(e.getMessage());
            story.setProcessingStatus(StoryDataStore.ProcessingStatus.FAILED);
            story.setErrorMessage(e.getMessage());
            storyDataStoreRepository.save(story);
            throw e;
        }
    }

    /**
     * Process paragraph rewrite
     */
    public void processParagraphRewrite(StoryDataStore story) {
        String storyId = story.getStoryId();
        try {
            log.info("Story {} - Step 1: Starting paragraph rewrite processing", storyId);

            // Step 1: Get the text to process (either from rewrite response or search text)
            String textToProcess = null;
            log.info("Story {} - Step 1.1: Determining text source for paragraph rewrite", storyId);

            if (story.getStoryRewriteResponse() != null && story.getStoryRewriteResponse().getRewrittenText() != null) {
                // Use rewritten text if available (for uploaded stories)
                textToProcess = story.getStoryRewriteResponse().getRewrittenText();
                log.info("Story {} - Step 1.1: Using rewritten text for paragraph rewrite ({} chars)", storyId,
                        textToProcess.length());
            } else if (story.getSearchText() != null && !story.getSearchText().trim().isEmpty()) {
                // Use search text for written stories
                textToProcess = story.getSearchText().trim();
                log.info("Story {} - Step 1.1: Using search text for paragraph rewrite ({} chars)", storyId,
                        textToProcess.length());
            } else {
                throw new RuntimeException(
                        "No text available for paragraph rewrite. Neither story rewrite response nor search text is available.");
            }

            log.info("Story {} - Step 1.1: Text validation successful", storyId);

            // Step 2: Call paragraph rewrite API
            log.info("Story {} - Step 2: Calling paragraph rewrite API", storyId);
            ParagraphRewriteResponse paragraphResponse = callParagraphRewriteApi(textToProcess);
            log.info("Story {} - Step 2: Paragraph rewrite API call successful", storyId);

            // Step 3: Update story with paragraph response
            log.info("Story {} - Step 3: Updating story with paragraph response", storyId);
            story.setParagraphRewriteResponse(paragraphResponse);
            story.setParagraphCompletedAt(LocalDateTime.now());
            storyDataStoreRepository.save(story);
            log.info("Story {} - Step 3: Story update successful", storyId);

            log.info("Story {} - SUCCESS: Paragraph rewrite processing completed successfully", storyId);

        } catch (Exception e) {
            log.error("Story {} - ERROR: Paragraph rewrite processing failed: {}", storyId, e.getMessage(), e);
            story.setParagraphError(e.getMessage());
            story.setProcessingStatus(StoryDataStore.ProcessingStatus.FAILED);
            story.setErrorMessage(e.getMessage());
            storyDataStoreRepository.save(story);
            throw e;
        }
    }

    /**
     * Convert paragraphs to Content list for mobile app
     */
    public List<Content> convertParagraphsToContent(ParagraphRewriteResponse paragraphResponse) {
        List<Content> contentList = new java.util.ArrayList<>();
        int orderIndex = 1;

        for (ParagraphRewriteResponse.Paragraph paragraph : paragraphResponse.getParagraphs()) {
            Content content = Content.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .type(Content.ContentType.TEXT)
                    .data(paragraph.getRewrittenText().trim())
                    .orderIndex(orderIndex)
                    .build();
            contentList.add(content);
            orderIndex += 2;
        }

        return contentList;
    }

    /**
     * Convert paragraphs to Content list with images for mobile app
     */
    public List<Content> convertParagraphsToContentWithImages(ParagraphRewriteResponse paragraphResponse,
            List<String> imageUrls) {
        List<Content> contentList = new java.util.ArrayList<>();
        int textOrderIndex = 0; // Even numbers for TEXT
        int imageUrlIndex = 0;

        for (ParagraphRewriteResponse.Paragraph paragraph : paragraphResponse.getParagraphs()) {
            String paragraphText = paragraph.getRewrittenText().trim();

            // Add TEXT content for each paragraph
            Content textContent = Content.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .type(Content.ContentType.TEXT)
                    .data(paragraphText)
                    .orderIndex(textOrderIndex)
                    .build();
            contentList.add(textContent);

            // Add IMAGE content if paragraph has > 100 characters, is not ambiguous, and we
            // have image URLs
            if (shouldGenerateImageForParagraph(paragraphText) && imageUrlIndex < imageUrls.size()) {
                Content imageContent = Content.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .type(Content.ContentType.IMAGE)
                        .data(imageUrls.get(imageUrlIndex))
                        .orderIndex(textOrderIndex + 1) // Odd number between TEXT paragraphs
                        .build();
                contentList.add(imageContent);
                imageUrlIndex++;
            }

            textOrderIndex += 2; // Move to next even number
        }

        // Sort by orderIndex to ensure proper ordering
        contentList.sort((c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()));

        return contentList;
    }

    /**
     * Determine if an image should be generated for a paragraph
     * Skip image generation for short or ambiguous paragraphs
     */
    private boolean shouldGenerateImageForParagraph(String paragraphText) {
        // Skip if paragraph is too short (< 100 characters)
        if (paragraphText.length() < 100) {
            log.debug("Skipping image generation for short paragraph ({} chars): {}",
                    paragraphText.length(), paragraphText.substring(0, Math.min(50, paragraphText.length())));
            return false;
        }

        // Skip if paragraph is ambiguous (contains uncertain content indicators)
        if (isAmbiguousParagraph(paragraphText)) {
            log.debug("Skipping image generation for ambiguous paragraph: {}",
                    paragraphText.substring(0, Math.min(50, paragraphText.length())));
            return false;
        }

        return true;
    }

    /**
     * Check if a paragraph is ambiguous or contains uncertain content
     */
    private boolean isAmbiguousParagraph(String paragraphText) {
        String lowerText = paragraphText.toLowerCase();

        // Keywords that indicate ambiguous or uncertain content
        String[] ambiguousKeywords = {
                "maybe", "perhaps", "possibly", "might", "could", "would", "should",
                "unclear", "uncertain", "unknown", "unsure", "doubt", "confused",
                "?", "??", "???", "what", "why", "how", "when", "where",
                "etc", "and so on", "and more", "similar", "like", "such as"
        };

        for (String keyword : ambiguousKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }

        // Check for very short sentences or incomplete thoughts
        if (paragraphText.split("\\.").length < 2 && paragraphText.length() < 150) {
            return true;
        }

        return false;
    }

    /**
     * Generate images from visual prompts using DALL-E
     */
    public List<String> generateImagesFromVisualPrompts(List<VisualPromptResponse.VisualPrompt> visualPrompts) {
        List<String> imageUrls = new java.util.ArrayList<>();

        for (VisualPromptResponse.VisualPrompt visualPrompt : visualPrompts) {
            try {
                // Check if paragraph is suitable for image generation
                if (shouldGenerateImageForParagraph(visualPrompt.getParagraphText())) {
                    // Generate image using the visual description
                    String imageUrl = generateImageFromVisualPrompt(visualPrompt.getVisualDescription());
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                        log.info("Successfully generated image for visual prompt");
                    } else {
                        // Image generation failed, but continue processing
                        log.warn("Image generation failed for visual prompt: {}. Continuing without this image.",
                                visualPrompt.getVisualDescription().substring(0,
                                        Math.min(50, visualPrompt.getVisualDescription().length())));
                    }
                } else {
                    log.debug("Skipping image generation for paragraph: {}",
                            visualPrompt.getParagraphText().substring(0,
                                    Math.min(50, visualPrompt.getParagraphText().length())));
                    // Don't add image URL for skipped paragraphs
                }
            } catch (Exception e) {
                log.warn("Failed to generate image for visual prompt: {}. Continuing without this image.",
                        e.getMessage());
                // Continue processing other images instead of failing the entire process
            }
        }

        return imageUrls;
    }

    /**
     * Generate image from visual prompt using DALL-E and upload to Cloudinary
     */
    private String generateImageFromVisualPrompt(String visualDescription) {
        try {
            log.info("Generating image from visual prompt: {}", visualDescription);

            // Call DALL-E API to generate image
            String dalleImageBase64 = callDalleApi(visualDescription);

            if (dalleImageBase64 != null) {
                // Upload the generated image to Cloudinary
                ImageUploadResponse uploadResponse = imageUploadService.uploadDalleImage(dalleImageBase64);

                if ("SUCCESS".equals(uploadResponse.getStatus())) {
                    log.info("Successfully uploaded DALL-E image to Cloudinary: {}", uploadResponse.getUrl());
                    return uploadResponse.getSecureUrl();
                } else {
                    log.warn("Failed to upload DALL-E image to Cloudinary: {}. Returning null to skip this image.",
                            uploadResponse.getError());
                    return null;
                }
            } else {
                log.warn("DALL-E API returned null image for visual prompt: {}. Returning null to skip this image.",
                        visualDescription.substring(0, Math.min(50, visualDescription.length())));
                return null;
            }

        } catch (Exception e) {
            log.warn("Error generating image from visual prompt: {}. Returning null to skip this image.",
                    e.getMessage());
            return null;
        }
    }

    /**
     * Call DALL-E API to generate image
     */
    public String callDalleApi(String visualDescription) {
        try {
            log.info("Calling DALL-E API for image generation");

            // Create DALL-E request
            Map<String, Object> dalleRequest = new HashMap<>();
            dalleRequest.put("model", "dall-e-3");
            dalleRequest.put("prompt", visualDescription);
            dalleRequest.put("n", 1);
            dalleRequest.put("size", "1024x1024");
            dalleRequest.put("response_format", "b64_json");

            // Call OpenAI DALL-E API
            Map<String, Object> response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/images/generations",
                    dalleRequest,
                    Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> imageData = data.get(0);
                    String base64Image = (String) imageData.get("b64_json");
                    log.info("Successfully generated image from DALL-E API");
                    return base64Image;
                }
            }

            log.warn("DALL-E API response was null or empty");
            return null;

        } catch (Exception e) {
            log.error("Error calling DALL-E API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate visual prompts for paragraphs
     */
    public VisualPromptResponse generateVisualPrompts(List<ParagraphRewriteResponse.Paragraph> paragraphs) {
        try {
            int totalParagraphs = paragraphs.size();
            log.info("Starting visual prompts generation for {} paragraphs", totalParagraphs);

            List<VisualPromptResponse.VisualPrompt> visualPrompts = new java.util.ArrayList<>();
            int successCount = 0;

            for (int i = 0; i < paragraphs.size(); i++) {
                int paragraphNumber = i + 1;
                ParagraphRewriteResponse.Paragraph paragraph = paragraphs.get(i);
                String paragraphText = paragraph.getRewrittenText();

                log.info("Processing paragraph {}/{} ({} chars): {}",
                        paragraphNumber, totalParagraphs, paragraphText.length(),
                        paragraphText.substring(0, Math.min(50, paragraphText.length())) + "...");

                try {
                    log.info("Paragraph {}/{} - Step 1: Generating visual prompt", paragraphNumber, totalParagraphs);
                    String visualDescription = generateVisualPromptForParagraph(paragraphText);
                    log.info("Paragraph {}/{} - Step 1: Visual prompt generated successfully", paragraphNumber,
                            totalParagraphs);

                    VisualPromptResponse.VisualPrompt visualPrompt = VisualPromptResponse.VisualPrompt.builder()
                            .paragraphNumber(paragraphNumber)
                            .paragraphText(paragraphText)
                            .visualDescription(visualDescription)
                            .style("animated")
                            .build();

                    visualPrompts.add(visualPrompt);
                    successCount++;
                    log.info("Paragraph {}/{} - SUCCESS: Visual prompt created", paragraphNumber, totalParagraphs);

                } catch (Exception e) {
                    log.error("Paragraph {}/{} - ERROR: Failed to generate visual prompt: {}", paragraphNumber,
                            totalParagraphs, e.getMessage());
                }
            }

            log.info("Visual prompts generation completed: {}/{} paragraphs processed successfully", successCount,
                    totalParagraphs);

            return VisualPromptResponse.builder()
                    .visualPrompts(visualPrompts)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("ERROR: Visual prompts generation failed: {}", e.getMessage());
            return VisualPromptResponse.builder()
                    .status("ERROR")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Generate visual prompt for a single paragraph
     */
    private String generateVisualPromptForParagraph(String paragraphText) {
        try {
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("visual_prompt_system");

            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("paragraphText", paragraphText);
            String userPrompt = promptConfig.formatPrompt("visual_prompt_user", params);

            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIConfig.getModel())
                    .messages(List.of(
                            OpenAIRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenAIRequest.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()))
                    .maxTokens(150)
                    .temperature(0.8)
                    .build();

            // Call OpenAI API
            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/chat/completions",
                    request,
                    OpenAIResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent().trim();
            }

        } catch (Exception e) {
            log.error("Error generating visual prompt for paragraph: {}", e.getMessage());
        }

        // Fallback visual description (no text, love/breakup theme)
        return promptConfig.getPrompt("visual_prompt_fallback");
    }

    /**
     * Process story analysis
     */
    public void processStoryAnalysis(StoryDataStore story) {
        String storyId = story.getStoryId();
        try {
            log.info("Story {} - Step 1: Starting story analysis processing", storyId);

            // Step 1: Get the text to analyze (either from rewrite response or search text)
            String textToAnalyze = null;
            log.info("Story {} - Step 1.1: Determining text source for analysis", storyId);

            if (story.getStoryRewriteResponse() != null && story.getStoryRewriteResponse().getRewrittenText() != null) {
                // Use rewritten text if available (for uploaded stories)
                textToAnalyze = story.getStoryRewriteResponse().getRewrittenText();
                log.info("Story {} - Step 1.1: Using rewritten text for analysis ({} chars)", storyId,
                        textToAnalyze.length());
            } else if (story.getSearchText() != null && !story.getSearchText().trim().isEmpty()) {
                // Use search text for written stories
                textToAnalyze = story.getSearchText().trim();
                log.info("Story {} - Step 1.1: Using search text for analysis ({} chars)", storyId,
                        textToAnalyze.length());
            } else {
                throw new RuntimeException(
                        "No text available for analysis. Neither story rewrite response nor search text is available.");
            }

            log.info("Story {} - Step 1.1: Text validation successful", storyId);

            // Step 2: Call story analysis API
            log.info("Story {} - Step 2: Calling story analysis API", storyId);
            StoryAnalysisResponse analysisResponse = callStoryAnalysisApi(textToAnalyze);
            log.info("Story {} - Step 2: Story analysis API call successful", storyId);

            // Step 3: Update story with analysis response and title
            log.info("Story {} - Step 3: Updating story with analysis response and title", storyId);
            story.setStoryAnalysisResponse(analysisResponse);

            // Set the generated title
            if (analysisResponse.getTitle() != null && !analysisResponse.getTitle().trim().isEmpty()) {
                story.setTitle(analysisResponse.getTitle());
                log.info("Story {} - Step 3.1: Title set to: {}", storyId, analysisResponse.getTitle());
            } else {
                log.warn("Story {} - Step 3.1: No title generated, using default", storyId);
                story.setTitle("A Love Story");
            }

            story.setAnalysisCompletedAt(LocalDateTime.now());
            storyDataStoreRepository.save(story);
            log.info("Story {} - Step 3: Story update successful", storyId);

            log.info("Story {} - SUCCESS: Story analysis processing completed successfully", storyId);

        } catch (Exception e) {
            log.error("Story {} - ERROR: Story analysis processing failed: {}", storyId, e.getMessage(), e);
            story.setAnalysisError(e.getMessage());
            story.setProcessingStatus(StoryDataStore.ProcessingStatus.FAILED);
            story.setErrorMessage(e.getMessage());
            storyDataStoreRepository.save(story);
            throw e;
        }
    }

    /**
     * Process visual prompts for story
     */
    public void processVisualPrompts(StoryDataStore story) {
        String storyId = story.getStoryId();
        try {
            log.info("Story {} - Step 1: Starting visual prompts processing", storyId);

            // Step 1: Validate paragraph rewrite response
            log.info("Story {} - Step 1.1: Validating paragraph rewrite response", storyId);
            if (story.getParagraphRewriteResponse() == null
                    || story.getParagraphRewriteResponse().getParagraphs() == null) {
                throw new RuntimeException(
                        "Paragraph rewrite response is null or empty. Cannot proceed with visual prompts.");
            }
            log.info("Story {} - Step 1.1: Paragraph rewrite validation successful", storyId);

            // Step 2: Generate visual prompts for paragraphs
            log.info("Story {} - Step 2: Generating visual prompts for {} paragraphs", storyId,
                    story.getParagraphRewriteResponse().getParagraphs().size());
            VisualPromptResponse visualPromptResponse = generateVisualPrompts(
                    story.getParagraphRewriteResponse().getParagraphs());
            log.info("Story {} - Step 2: Visual prompts generation successful", storyId);

            // Step 3: Update story with visual prompt response
            log.info("Story {} - Step 3: Updating story with visual prompt response", storyId);
            story.setVisualPromptResponse(visualPromptResponse);
            story.setVisualPromptCompletedAt(LocalDateTime.now());
            storyDataStoreRepository.save(story);
            log.info("Story {} - Step 3: Story update successful", storyId);

            log.info("Story {} - SUCCESS: Visual prompts processing completed successfully", storyId);

        } catch (Exception e) {
            log.error("Story {} - ERROR: Visual prompts processing failed: {}", storyId, e.getMessage(), e);
            story.setVisualPromptError(e.getMessage());
            story.setProcessingStatus(StoryDataStore.ProcessingStatus.FAILED);
            story.setErrorMessage(e.getMessage());
            storyDataStoreRepository.save(story);
            throw e;
        }
    }

    /**
     * Call OpenAI API for story rewrite in Indian languages
     */
    public StoryRewriteResponse callStoryRewriteApi(String transcription) {
        log.info("Step 1: Preparing OpenAI API request for story rewrite");
        log.info("Step 1.1: Input transcription length: {} characters", transcription.length());

        try {
            log.info("Step 2: Building system prompt for story rewrite");
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("story_rewrite_system");

            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("transcription", transcription);
            String userPrompt = promptConfig.formatPrompt("story_rewrite_user", params);

            log.info("Step 3: Creating OpenAI request with model: {}", openAIConfig.getModel());
            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIConfig.getModel())
                    .messages(List.of(
                            OpenAIRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenAIRequest.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()))
                    .maxTokens(openAIConfig.getMaxTokens())
                    .temperature(openAIConfig.getTemperature())
                    .build();

            log.info("Step 4: Calling OpenAI API endpoint: {}", openAIConfig.getBaseUrl() + "/chat/completions");
            // Call OpenAI API
            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/chat/completions",
                    request,
                    OpenAIResponse.class);

            log.info("Step 5: Processing OpenAI API response");
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String rewrittenText = response.getChoices().get(0).getMessage().getContent();
                log.info("Step 5.1: Rewritten text length: {} characters", rewrittenText.length());

                log.info("Step 6: Building story rewrite response");
                StoryRewriteResponse result = StoryRewriteResponse.builder()
                        .originalText(transcription)
                        .rewrittenText(rewrittenText)
                        .style("emotional")
                        .tone("breakup-love")
                        .status("SUCCESS")
                        .build();

                log.info("SUCCESS: Story rewrite API call completed successfully");
                return result;
            } else {
                log.error("ERROR: Invalid response from OpenAI API - response is null or empty");
                throw new RuntimeException("Invalid response from OpenAI API");
            }

        } catch (Exception e) {
            log.error("ERROR: OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Call OpenAI API for paragraph rewrite with specific prompts
     */
    public ParagraphRewriteResponse callParagraphRewriteApi(String rewrittenText) {
        log.info("Calling OpenAI API for paragraph rewrite with specific prompts");

        try {
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("paragraph_rewrite_system");

            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("rewrittenText", rewrittenText);
            String userPrompt = promptConfig.formatPrompt("paragraph_rewrite_user", params);

            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIConfig.getModel())
                    .messages(List.of(
                            OpenAIRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenAIRequest.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()))
                    .maxTokens(openAIConfig.getMaxTokens())
                    .temperature(openAIConfig.getTemperature())
                    .build();

            // Call OpenAI API
            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/chat/completions",
                    request,
                    OpenAIResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String paragraphText = response.getChoices().get(0).getMessage().getContent();

                // Split into paragraphs and create response
                String[] paragraphs = paragraphText.split("\n\n");
                List<ParagraphRewriteResponse.Paragraph> paragraphList = new java.util.ArrayList<>();

                for (int i = 0; i < paragraphs.length; i++) {
                    if (!paragraphs[i].trim().isEmpty()) {
                        paragraphList.add(ParagraphRewriteResponse.Paragraph.builder()
                                .originalText(rewrittenText)
                                .rewrittenText(paragraphs[i].trim())
                                .paragraphNumber(i + 1)
                                .style("emotional")
                                .build());
                    }
                }

                return ParagraphRewriteResponse.builder()
                        .paragraphs(paragraphList)
                        .status("SUCCESS")
                        .build();
            } else {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

        } catch (Exception e) {
            log.error("Error calling OpenAI API for paragraph rewrite: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Call OpenAI API for story analysis
     */
    public StoryAnalysisResponse callStoryAnalysisApi(String rewrittenText) {
        log.info("Step 1: Preparing OpenAI API request for story analysis");
        log.info("Step 1.1: Input story length: {} characters", rewrittenText.length());

        try {
            log.info("Step 2: Building system prompt for story analysis");
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("story_analysis_system");

            log.info(
                    "Step 3: Building user prompt with INTERESTING title generation (60 chars max, open-ended questions)");
            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("rewrittenText", rewrittenText);
            String userPrompt = promptConfig.formatPrompt("story_analysis_user", params);

            OpenAIRequest request = OpenAIRequest.builder()
                    .model(openAIConfig.getModel())
                    .messages(List.of(
                            OpenAIRequest.Message.builder()
                                    .role("system")
                                    .content(systemPrompt)
                                    .build(),
                            OpenAIRequest.Message.builder()
                                    .role("user")
                                    .content(userPrompt)
                                    .build()))
                    .maxTokens(openAIConfig.getMaxTokens())
                    .temperature(openAIConfig.getTemperature())
                    .build();

            // Call OpenAI API
            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openAIConfig.getBaseUrl() + "/chat/completions",
                    request,
                    OpenAIResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String analysisText = response.getChoices().get(0).getMessage().getContent();

                // Parse the JSON response
                return parseStoryAnalysisResponse(analysisText);

            } else {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

        } catch (Exception e) {
            log.error("Error calling OpenAI API for story analysis: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse the story analysis response from OpenAI
     */
    private StoryAnalysisResponse parseStoryAnalysisResponse(String analysisText) {
        log.info("Parsing story analysis response: {}", analysisText);

        // Clean the response - remove markdown code blocks and whitespace
        String cleanedText = analysisText.trim();
        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.substring(7).trim();
        }
        if (cleanedText.startsWith("```")) {
            cleanedText = cleanedText.substring(3).trim();
        }
        if (cleanedText.endsWith("```")) {
            cleanedText = cleanedText.substring(0, cleanedText.length() - 3).trim();
        }
        cleanedText = cleanedText.trim();

        try {
            JsonNode root = objectMapper.readTree(cleanedText);
            JsonNode analysisNode = root.has("analysis") ? root.get("analysis") : root;

            // Parse title (new field)
            String title = root.has("title") ? root.get("title").asText() : null;
            if (title == null || title.isEmpty()) {
                log.warn("Title not found in analysis response, using default");
                title = "A Love Story";
            }
            log.info("Step 6.1: Parsed title: '{}' ({} characters)", title, title.length());

            // Parse required fields
            JsonNode emotionsNode = analysisNode.get("emotions_with_scores");
            if (emotionsNode == null || !emotionsNode.isObject())
                throw new RuntimeException("Could not parse emotions_with_scores from analysis response");
            Map<String, Double> emotionsWithScores = new HashMap<>();
            emotionsNode.fields().forEachRemaining(e -> emotionsWithScores.put(e.getKey(), e.getValue().asDouble()));

            String storyType = analysisNode.has("story_type") ? analysisNode.get("story_type").asText() : null;
            if (storyType == null || storyType.isEmpty())
                throw new RuntimeException("Could not parse story_type from analysis response");

            Boolean isValidStory = analysisNode.has("is_valid_story") ? analysisNode.get("is_valid_story").asBoolean()
                    : null;
            if (isValidStory == null)
                throw new RuntimeException("Could not parse is_valid_story from analysis response");

            String plotSummary = analysisNode.has("plot_summary") ? analysisNode.get("plot_summary").asText() : null;
            if (plotSummary == null || plotSummary.isEmpty())
                throw new RuntimeException("Could not parse plot_summary from analysis response");

            // Optional fields
            List<String> tags = new ArrayList<>();
            if (analysisNode.has("tags") && analysisNode.get("tags").isArray()) {
                analysisNode.get("tags").forEach(n -> tags.add(n.asText()));
            }
            List<String> locations = new ArrayList<>();
            if (analysisNode.has("locations") && analysisNode.get("locations").isArray()) {
                analysisNode.get("locations").forEach(n -> locations.add(n.asText()));
            }
            List<String> themes = new ArrayList<>();
            if (analysisNode.has("themes") && analysisNode.get("themes").isArray()) {
                analysisNode.get("themes").forEach(n -> themes.add(n.asText()));
            }
            List<String> culturalElements = new ArrayList<>();
            if (analysisNode.has("cultural_elements") && analysisNode.get("cultural_elements").isArray()) {
                analysisNode.get("cultural_elements").forEach(n -> culturalElements.add(n.asText()));
            }
            List<StoryAnalysisResponse.NameInfo> names = new ArrayList<>();
            if (analysisNode.has("names") && analysisNode.get("names").isArray()) {
                for (JsonNode nameNode : analysisNode.get("names")) {
                    String name = nameNode.has("name") ? nameNode.get("name").asText() : null;
                    String role = nameNode.has("role") ? nameNode.get("role").asText() : null;
                    String gender = nameNode.has("gender") ? nameNode.get("gender").asText() : null;
                    if (name != null) {
                        names.add(StoryAnalysisResponse.NameInfo.builder()
                                .name(name)
                                .role(role)
                                .gender(gender)
                                .build());
                    }
                }
            }
            if (themes.isEmpty())
                throw new RuntimeException("Could not parse themes from analysis response");

            StoryAnalysisResponse.Analysis analysis = StoryAnalysisResponse.Analysis.builder()
                    .emotionsWithScores(emotionsWithScores)
                    .tags(tags)
                    .locations(locations)
                    .names(names)
                    .storyType(storyType)
                    .isValidStory(isValidStory)
                    .themes(themes)
                    .plotSummary(plotSummary)
                    .culturalElements(culturalElements)
                    .build();

            return StoryAnalysisResponse.builder()
                    .success(true)
                    .title(title)
                    .analysis(analysis)
                    .error(null)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse story analysis response with Jackson: {}", e.getMessage());
            throw new RuntimeException("Failed to parse story analysis response: " + e.getMessage(), e);
        }
    }

}