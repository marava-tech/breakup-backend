package com.breakupstories.config;

import com.breakupstories.model.DefaultConfig;
import com.breakupstories.repository.DefaultConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializationConfig implements CommandLineRunner {
    
    private final DefaultConfigRepository defaultConfigRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultProfileImages();
    }
    
    private void initializeDefaultProfileImages() {
        log.info("Initializing default profile image configurations...");
        
        // Default male profile image
        initializeConfigIfNotExists(
            "DefaultMaleProfileImageUrl",
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/default_male_profile.png",
            "Default profile image URL for male users"
        );
        
        // Default female profile image
        initializeConfigIfNotExists(
            "DefaultFemaleProfileImageUrl",
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/default_female_profile.png",
            "Default profile image URL for female users"
        );
        
        log.info("Default profile image configurations initialized successfully");
    }
    
    private void initializeConfigIfNotExists(String key, String value, String description) {
        if (!defaultConfigRepository.existsByKey(key)) {
            DefaultConfig config = DefaultConfig.builder()
                    .key(key)
                    .value(value)
                    .description(description)
                    .active(true)
                    .build();
            
            defaultConfigRepository.save(config);
            log.info("Created default config: {} = {}", key, value);
        } else {
            log.debug("Default config already exists: {}", key);
        }
    }
} 