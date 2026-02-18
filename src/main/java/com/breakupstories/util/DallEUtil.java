package com.breakupstories.util;

import com.breakupstories.config.OpenAIConfig;
import com.breakupstories.dto.DallERequest;
import com.breakupstories.dto.DallEResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for DALL-E image generation
 */
@Component
public class DallEUtil {

    private static final Logger log = LoggerFactory.getLogger(DallEUtil.class);

    private final OpenAIConfig openAIConfig;
    private final RestTemplate openaiRestTemplate;

    public DallEUtil(OpenAIConfig openAIConfig, RestTemplate openaiRestTemplate) {
        this.openAIConfig = openAIConfig;
        this.openaiRestTemplate = openaiRestTemplate;
    }

    /**
     * Generate image using DALL-E API
     *
     * @param prompt The image generation prompt
     * @return Byte array of the generated image
     * @throws IOException If image generation fails
     */
    public byte[] generateImage(String prompt) throws IOException {
        log.info("Generating image with DALL-E for prompt: {}", prompt);

        try {
            // Create DALL-E request
            DallERequest request = DallERequest.builder()
                    .model("dall-e-3")
                    .prompt(prompt)
                    .n(1)
                    .size("1024x1024")
                    .quality("standard")
                    .responseFormat("url")
                    .style("vivid")
                    .build();

            // Make API call
            String url = openAIConfig.getBaseUrl() + "/images/generations";
            DallEResponse response = openaiRestTemplate.postForObject(url, request, DallEResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                throw new IOException("No image data received from DALL-E API");
            }

            // Download the image from the URL
            String imageUrl = response.getData().getFirst().getUrl();
            byte[] imageData = downloadImageFromUrl(imageUrl);

            log.info("Successfully generated image with size: {} bytes", imageData.length);
            return imageData;

        } catch (Exception e) {
            log.error("Error generating image with DALL-E: {}", e.getMessage(), e);
            throw new IOException("Failed to generate image: " + e.getMessage(), e);
        }
    }

    /**
     * Download image from URL
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        try {
            URL url = new URL(imageUrl);
            return url.openStream().readAllBytes();
        } catch (Exception e) {
            log.error("Error downloading image from URL: {}", imageUrl, e);
            throw new IOException("Failed to download image: " + e.getMessage(), e);
        }
    }
}
