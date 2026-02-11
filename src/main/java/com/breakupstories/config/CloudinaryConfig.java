package com.breakupstories.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${app.cloudinary.cloud-name:dhssmiyoc}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:REDACTED_CLOUDINARY_KEY}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:REDACTED_CLOUDINARY_SECRET}")
    private String apiSecret;

    @Bean
    Cloudinary getCloudinary() {
        Map<String, String> map = new HashMap<>();

        map.put("cloud_name", cloudName);
        map.put("api_key", apiKey);
        map.put("api_secret", apiSecret);
        map.put("secure", "true");

        // Performance optimizations
        map.put("timeout", "60000"); // 60 seconds timeout
        map.put("connection_timeout", "60000"); // 60 seconds connection timeout

        log.info("Cloudinary configured with cloud name: {}", cloudName);
        return new Cloudinary(map);
    }
}
