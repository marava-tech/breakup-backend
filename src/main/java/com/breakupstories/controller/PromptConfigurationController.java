package com.breakupstories.controller;

import com.breakupstories.service.PromptConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing prompts through configuration portal
 */
@RestController
@RequestMapping("/api/prompts")
public class PromptConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(PromptConfigurationController.class);

    private final PromptConfigurationService promptConfigurationService;

    public PromptConfigurationController(PromptConfigurationService promptConfigurationService) {
        this.promptConfigurationService = promptConfigurationService;
    }

    /**
     * Get all prompts
     * @return Map of all prompt keys and values
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getAllPrompts() {
        log.info("Getting all prompts");
        // This would typically return prompts from your configuration portal
        // For now, returning a sample structure
        Map<String, String> prompts = new HashMap<>();
        prompts.put("visual_prompt_system", promptConfigurationService.getPrompt("visual_prompt_system"));
        prompts.put("visual_prompt_user", promptConfigurationService.getPrompt("visual_prompt_user"));
        prompts.put("story_rewrite_system", promptConfigurationService.getPrompt("story_rewrite_system"));
        prompts.put("story_analysis_system", promptConfigurationService.getPrompt("story_analysis_system"));
        prompts.put("consoling_system", promptConfigurationService.getPrompt("consoling_system"));
        prompts.put("abuse_detection_system", promptConfigurationService.getPrompt("abuse_detection_system"));
        return ResponseEntity.ok(prompts);
    }

    /**
     * Get a specific prompt by key
     * @param key The prompt key
     * @return The prompt value
     */
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, String>> getPrompt(@PathVariable String key) {
        log.info("Getting prompt for key: {}", key);
        String prompt = promptConfigurationService.getPrompt(key);
        Map<String, String> response = new HashMap<>();
        response.put("key", key);
        response.put("value", prompt);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a prompt
     * @param key The prompt key
     * @param request The request containing the new prompt value
     * @return Success response
     */
    @PutMapping("/{key}")
    public ResponseEntity<Map<String, String>> updatePrompt(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {

        log.info("Updating prompt for key: {}", key);
        String newValue = request.get("value");

        if (newValue == null || newValue.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Prompt value cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }

        promptConfigurationService.updatePrompt(key, newValue.trim());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Prompt updated successfully");
        response.put("key", key);
        return ResponseEntity.ok(response);
    }

    /**
     * Reload prompts from configuration portal
     * @return Success response
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reloadPrompts() {
        log.info("Reloading prompts from configuration portal");
        promptConfigurationService.reloadPrompts();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Prompts reloaded successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Test a prompt with parameters
     * @param key The prompt key
     * @param request The request containing parameters
     * @return The formatted prompt
     */
    @PostMapping("/{key}/test")
    public ResponseEntity<Map<String, String>> testPrompt(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {

        log.info("Testing prompt for key: {}", key);

        try {
            String formattedPrompt = promptConfigurationService.formatPrompt(key, request);

            Map<String, String> response = new HashMap<>();
            response.put("key", key);
            response.put("formatted_prompt", formattedPrompt);
            response.put("parameters", request.toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error testing prompt: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to format prompt: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get available prompt keys
     * @return List of available prompt keys
     */
    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> getPromptKeys() {
        log.info("Getting available prompt keys");

        Map<String, Object> response = new HashMap<>();
        response.put("available_keys", new String[]{
            "visual_prompt_system",
            "visual_prompt_user",
            "visual_prompt_fallback",
            "image_prompt_system",
            "image_prompt_user",
            "image_prompt_fallback",
            "story_rewrite_system",
            "story_rewrite_user",
            "paragraph_rewrite_system",
            "paragraph_rewrite_user",
            "story_analysis_system",
            "story_analysis_user",
            "consoling_system",
            "consoling_user",
            "abuse_detection_system",
            "abuse_detection_user",
            "thumbnail_generation",
            "analysis_context_fallback"
        });

        return ResponseEntity.ok(response);
    }
}
