package com.breakupstories.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.List;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing prompts from MongoDB default_configs collection
 * This service provides a centralized way to access prompts instead of hardcoding them
 */
@Service
public class PromptConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(PromptConfigurationService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "default_configs";

    /**
     * Get a prompt by key from MongoDB default_configs collection
     * @param key The prompt key
     * @return The prompt value
     */
    public String getPrompt(String key) {
        try {
            Query query = new Query(Criteria.where("key").is(key));
            Map<String, Object> config = mongoTemplate.findOne(query, Map.class, COLLECTION_NAME);

            if (config != null && config.containsKey("value")) {
                String prompt = (String) config.get("value");
                if (prompt != null && !prompt.trim().isEmpty()) {
                    return prompt;
                }
            }

            log.warn("Prompt not found for key: {}", key);
            return getDefaultPrompt(key);
        } catch (Exception e) {
            log.error("Error fetching prompt for key: {}", key, e);
            return getDefaultPrompt(key);
        }
    }

    /**
     * Get a prompt by key with fallback
     * @param key The prompt key
     * @param fallback The fallback value if prompt not found
     * @return The prompt value or fallback
     */
    public String getPrompt(String key, String fallback) {
        try {
            Query query = new Query(Criteria.where("key").is(key));
            Map<String, Object> config = mongoTemplate.findOne(query, Map.class, COLLECTION_NAME);

            if (config != null && config.containsKey("value")) {
                String prompt = (String) config.get("value");
                if (prompt != null && !prompt.trim().isEmpty()) {
                    return prompt;
                }
            }

            return fallback;
        } catch (Exception e) {
            log.error("Error fetching prompt for key: {}", key, e);
            return fallback;
        }
    }

    /**
     * Update a prompt in MongoDB
     * @param key The prompt key
     * @param value The new prompt value
     */
    public void updatePrompt(String key, String value) {
        try {
            Query query = new Query(Criteria.where("key").is(key));
            Update update = new Update().set("value", value).set("updated_at", new java.util.Date());

            mongoTemplate.upsert(query, update, COLLECTION_NAME);
            log.info("Updated prompt for key: {}", key);
        } catch (Exception e) {
            log.error("Error updating prompt for key: {}", key, e);
            throw new RuntimeException("Failed to update prompt: " + e.getMessage(), e);
        }
    }

    /**
     * Reload prompts from MongoDB
     * This method can be used to refresh prompts if needed
     */
    public void reloadPrompts() {
        log.info("Reloading prompts from MongoDB");
        // MongoDB queries are real-time, so no caching needed
        // This method can be used for any additional reload logic if needed
    }

    /**
     * Check if a prompt exists in MongoDB
     * @param key The prompt key
     * @return true if prompt exists, false otherwise
     */
    public boolean promptExists(String key) {
        try {
            Query query = new Query(Criteria.where("key").is(key));
            return mongoTemplate.exists(query, COLLECTION_NAME);
        } catch (Exception e) {
            log.error("Error checking if prompt exists for key: {}", key, e);
            return false;
        }
    }

    /**
     * Get all prompt keys from MongoDB
     * @return List of all prompt keys
     */
    public List<String> getAllPromptKeys() {
        try {
            Query query = new Query();
            query.fields().include("key");

            List<String> keys = new java.util.ArrayList<>();
            List<Map> configs = mongoTemplate.find(query, Map.class, COLLECTION_NAME);

            for (Map config : configs) {
                Object keyObj = config.get("key");
                if (keyObj != null) {
                    keys.add(keyObj.toString());
                }
            }

            return keys;
        } catch (Exception e) {
            log.error("Error fetching all prompt keys", e);
            return new java.util.ArrayList<>();
        }
    }



    /**
     * Get default prompt for a key (fallback when prompt not found)
     * @param key The prompt key
     * @return A default prompt or empty string
     */
    private String getDefaultPrompt(String key) {
        // Return appropriate default based on key patte
        if (key.contains("system")) {
            return "You are an AI assistant.";
        } else if (key.contains("fallback")) {
            return "Default fallback response.";
        } else {
            return "Please provide the requested information.";
        }
    }

    /**
     * Format a prompt template with parameters
     * @param key The prompt key
     * @param params The parameters to substitute
     * @return The formatted prompt
     */
    public String formatPrompt(String key, Map<String, String> params) {
        String prompt = getPrompt(key);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            prompt = prompt.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return prompt;
    }
}
