package com.breakupstories.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAI API
 */
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    private Api api = new Api();

    public static class Api {
        private String key;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o";
        private int maxTokens = 16000;
        private double temperature = 0.7;

        // Manual getters and setters for Lombok compatibility
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }

    public Api getApi() { return api; }
    public void setApi(Api api) { this.api = api; }

    public String getApiKey() {
        return api.getKey();
    }

    public String getBaseUrl() {
        return api.getBaseUrl();
    }

    public String getModel() {
        return api.getModel();
    }

    public int getMaxTokens() {
        return api.getMaxTokens();
    }

    public double getTemperature() {
        return api.getTemperature();
    }
}
