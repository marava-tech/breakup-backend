package com.breakupstories.service;

import com.breakupstories.config.OpenAIConfig;
import com.breakupstories.dto.ConsolingMessageResponse;
import com.breakupstories.dto.OpenAIRequest;
import com.breakupstories.dto.OpenAIResponse;
import com.breakupstories.model.StoryDataStore;
import com.breakupstories.model.User;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.breakupstories.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service for generating consoling messages using AI
 */
@Service
public class ConsolingService {

    private static final Logger log = LoggerFactory.getLogger(ConsolingService.class);

    private final StoryDataStoreRepository storyRepository;
    private final UserRepository userRepository;
    private final OpenAIConfig openAIConfig;
    private final PromptConfigurationService promptConfig;

    @Autowired
    private RestTemplate openaiRestTemplate;

    public ConsolingService(StoryDataStoreRepository storyRepository, UserRepository userRepository,
            OpenAIConfig openAIConfig, PromptConfigurationService promptConfig) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.openAIConfig = openAIConfig;
        this.promptConfig = promptConfig;
    }

    /**
     * Generate consoling message for a story
     */
    public ConsolingMessageResponse generateConsolingMessage(String storyId, String persona) {
        log.info("Generating consoling message for story: {} with persona: {}", storyId, persona);

        try {
            // 1. Fetch story by storyId
            StoryDataStore story = storyRepository.findByStoryId(storyId)
                    .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));

            // 2. Validate story has rewritten content
            if (story.getStoryRewriteResponse() == null ||
                    story.getStoryRewriteResponse().getRewrittenText() == null ||
                    story.getStoryRewriteResponse().getRewrittenText().trim().isEmpty()) {
                throw new RuntimeException("Story has no rewritten content");
            }

            // 3. Fetch user by userId
            User user = userRepository.findById(story.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + story.getUserId()));

            // 4. Generate consoling message using AI
            String genderStr = user.getGender() != null ? user.getGender().toString() : "unknown";
            String consolingMessage = generateConsolingMessageWithAI(
                    persona,
                    user.getName(),
                    user.getAge(),
                    genderStr,
                    user.getPreferredStoryLanguage(),
                    story.getStoryRewriteResponse().getRewrittenText());

            // 5. Build response
            ConsolingMessageResponse.UserDetails userDetails = ConsolingMessageResponse.UserDetails.builder()
                    .name(user.getName())
                    .age(user.getAge())
                    .gender(genderStr)
                    .preferredStoryLanguage(user.getPreferredStoryLanguage())
                    .build();

            return ConsolingMessageResponse.builder()
                    .storyId(storyId)
                    .userId(story.getUserId())
                    .persona(persona)
                    .user(userDetails)
                    .consolingMessage(consolingMessage)
                    .build();

        } catch (Exception e) {
            log.error("Error generating consoling message for story: {}", storyId, e);
            throw new RuntimeException("Failed to generate consoling message: " + e.getMessage(), e);
        }
    }

    /**
     * Generate consoling message using OpenAI API
     */
    private String generateConsolingMessageWithAI(String persona, String name, Integer age,
            String gender, String preferredLanguage, String rewrittenStory) {
        log.info("Calling OpenAI API for consoling message generation");

        try {
            // Get prompts from configuration
            String systemPrompt = promptConfig.getPrompt("consoling_system");

            // Prepare parameters for user prompt
            Map<String, String> params = new HashMap<>();
            params.put("persona", persona);
            params.put("name", name);
            params.put("age", age.toString());
            params.put("gender", gender);
            params.put("preferredLanguage", preferredLanguage);
            params.put("rewrittenStory", rewrittenStory);

            String userPrompt = promptConfig.formatPrompt("consoling_user", params);

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
                String consolingMessage = response.getChoices().get(0).getMessage().getContent().trim();
                log.info("Successfully generated consoling message");
                return consolingMessage;
            } else {
                throw new RuntimeException("Invalid response from OpenAI API");
            }

        } catch (Exception e) {
            log.error("Error calling OpenAI API for consoling message: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }
}