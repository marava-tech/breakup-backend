package com.breakupstories.controller;

import com.breakupstories.dto.RecStartAudioResponse;
import com.breakupstories.dto.QuoteResponse;
import com.breakupstories.model.User;
import com.breakupstories.service.DefaultConfigService;
import com.breakupstories.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audio", description = "Audio-related APIs")
public class AudioController {
    
    private final DefaultConfigService defaultConfigService;
    private final UserService userService;
    
    @GetMapping("/rec-start-audio")
    @Operation(summary = "Get recording start audio", description = "Get recording start audio URL based on user's preferred language and gender")
    public ResponseEntity<RecStartAudioResponse> getRecStartAudio(Authentication authentication) {
        
        User user = userService.getUserEntityByEmail(authentication.getName());
        String language = user.getPreferredStoryLanguage().toLowerCase();
        String gender = user.getGender().name().toLowerCase();
        
        log.info("Recording start audio request for language: {} and gender: {}", language, gender);
        
        try {
            RecStartAudioResponse response = defaultConfigService.getRecStartAudio(language, gender);
            log.info("Successfully retrieved recording start audio for language: {} and gender: {}", language, gender);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get recording start audio for language: {} and gender: {}", language, gender, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/quotes")
    @Operation(summary = "Get random quote combinations", description = "Get random combinations of quote text, audio, and image for creating videos")
    public ResponseEntity<List<QuoteResponse>> getQuotes(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Quote combinations request with limit: {}", limit);
        
        try {
            List<QuoteResponse> quotes = defaultConfigService.getRandomQuotes(limit);
            log.info("Successfully retrieved {} quote combinations", quotes.size());
            return ResponseEntity.ok(quotes);
        } catch (Exception e) {
            log.error("Failed to get quote combinations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 