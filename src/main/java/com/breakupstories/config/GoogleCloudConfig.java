package com.breakupstories.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GoogleCloudConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudConfig.class);

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.credentials.path}")
    private String credentialsPath;

    private SpeechClient speechClient;
    private TextToSpeechClient textToSpeechClient;

    /**
     * Load Google Cloud credentials from classpath
     */
    private GoogleCredentials loadCredentials() throws IOException {
        String resourcePath = credentialsPath.replace("classpath:", "");
        try (InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (credentialsStream == null) {
                log.error("Could not find Google Cloud credentials file: {}", resourcePath);
                throw new RuntimeException("Google Cloud credentials file not found: " + resourcePath);
            }
            return GoogleCredentials.fromStream(credentialsStream);
        }
    }

    @Bean
    public SpeechClient speechClient() throws IOException {
        try {
            GoogleCredentials credentials = loadCredentials();

            // Create SpeechClient with explicit credentials and project ID
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

            this.speechClient = SpeechClient.create(speechSettings);

            log.info("Google Cloud Speech-to-Text client initialized successfully");
            return this.speechClient;
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Speech-to-Text client: {}", e.getMessage());
            throw new RuntimeException("Google Cloud Speech-to-Text initialization failed", e);
        }
    }

    @Bean
    public TextToSpeechClient textToSpeechClient() throws IOException {
        try {
            GoogleCredentials credentials = loadCredentials();

            // Create TextToSpeechClient with explicit credentials
            TextToSpeechSettings textToSpeechSettings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

            this.textToSpeechClient = TextToSpeechClient.create(textToSpeechSettings);

            log.info("Google Cloud Text-to-Speech client initialized successfully");
            return this.textToSpeechClient;
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Text-to-Speech client: {}", e.getMessage());
            throw new RuntimeException("Google Cloud Text-to-Speech initialization failed", e);
        }
    }

    @Bean
    public Storage storageClient() throws IOException {
        try {
            GoogleCredentials credentials = loadCredentials();

            // Create Storage client with explicit credentials and project ID
            StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build();

            Storage storage = storageOptions.getService();

            log.info("Google Cloud Storage client initialized successfully");
            return storage;
        } catch (Exception e) {
            log.error("Failed to initialize Google Cloud Storage client: {}", e.getMessage());
            throw new RuntimeException("Google Cloud Storage initialization failed", e);
        }
    }

    /**
     * Cleanup method to properly close Google Cloud clients on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (speechClient != null) {
            try {
                speechClient.close();
                log.info("Google Cloud Speech-to-Text client closed successfully");
            } catch (Exception e) {
                log.error("Error closing Speech client: {}", e.getMessage());
            }
        }

        if (textToSpeechClient != null) {
            try {
                textToSpeechClient.close();
                log.info("Google Cloud Text-to-Speech client closed successfully");
            } catch (Exception e) {
                log.error("Error closing Text-to-Speech client: {}", e.getMessage());
            }
        }
    }
}
