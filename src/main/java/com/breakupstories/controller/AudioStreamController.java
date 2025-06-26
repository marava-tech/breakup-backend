package com.breakupstories.controller;

import com.breakupstories.dto.AudioInfoResponse;
import com.breakupstories.service.AuditService;
import com.breakupstories.service.AudioStreamService;
import com.breakupstories.service.ClientInfoService;
import com.breakupstories.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audio Streaming", description = "Audio streaming APIs for stories")
public class AudioStreamController {
    
    private final AudioStreamService audioStreamService;
    private final AuditService auditService;
    private final ClientInfoService clientInfoService;
    private final UserService userService;
    
    @GetMapping("/stream/{storyId}")
    @Operation(summary = "Stream audio", description = "Stream audio file for a story with support for partial content requests")
    public ResponseEntity<Resource> streamAudio(
            @PathVariable String storyId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            Authentication authentication) {
        
        log.info("Audio stream request for story: {} with range: {}", storyId, rangeHeader);
        
        // Audit audio play event if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            String userId = userService.getUserEntityByEmail(email).getId();
            
            ClientInfoService.ClientInfo clientInfo = clientInfoService.extractClientInfo();
            auditService.logAudioPlay(userId, storyId, clientInfo.getUserAgent(), 
                                    clientInfo.getIpAddress(), clientInfo.getSessionId(), 
                                    null, null);
            log.info("Audited audio play for user {} on story {}", userId, storyId);
        }
        
        return audioStreamService.streamAudio(storyId, rangeHeader);
    }
    
    @GetMapping("/info/{storyId}")
    @Operation(summary = "Get audio info", description = "Get audio file information for a story")
    public ResponseEntity<AudioInfoResponse> getAudioInfo(@PathVariable String storyId) {
        log.info("Audio info request for story: {}", storyId);
        AudioInfoResponse info = audioStreamService.getAudioInfo(storyId);
        return ResponseEntity.ok(info);
    }
    
    @PostMapping("/play/{storyId}")
    @Operation(summary = "Log audio play event", description = "Log when user starts playing audio")
    public ResponseEntity<Void> logAudioPlay(
            @PathVariable String storyId,
            @RequestParam(required = false) Long duration,
            @RequestParam(required = false) Long position,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();
        
        ClientInfoService.ClientInfo clientInfo = clientInfoService.extractClientInfo();
        auditService.logAudioPlay(userId, storyId, clientInfo.getUserAgent(), 
                                clientInfo.getIpAddress(), clientInfo.getSessionId(), 
                                duration, position);
        log.info("Audited audio play event for user {} on story {} at position {}", userId, storyId, position);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/pause/{storyId}")
    @Operation(summary = "Log audio pause event", description = "Log when user pauses audio")
    public ResponseEntity<Void> logAudioPause(
            @PathVariable String storyId,
            @RequestParam(required = false) Long duration,
            @RequestParam(required = false) Long position,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();
        
        ClientInfoService.ClientInfo clientInfo = clientInfoService.extractClientInfo();
        auditService.logAudioPause(userId, storyId, clientInfo.getUserAgent(), 
                                 clientInfo.getIpAddress(), clientInfo.getSessionId(), 
                                 duration, position);
        log.info("Audited audio pause event for user {} on story {} at position {}", userId, storyId, position);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/stop/{storyId}")
    @Operation(summary = "Log audio stop event", description = "Log when user stops audio")
    public ResponseEntity<Void> logAudioStop(
            @PathVariable String storyId,
            @RequestParam(required = false) Long duration,
            @RequestParam(required = false) Long position,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();
        
        ClientInfoService.ClientInfo clientInfo = clientInfoService.extractClientInfo();
        auditService.logAudioStop(userId, storyId, clientInfo.getUserAgent(), 
                                clientInfo.getIpAddress(), clientInfo.getSessionId(), 
                                duration, position);
        log.info("Audited audio stop event for user {} on story {} at position {}", userId, storyId, position);
        
        return ResponseEntity.ok().build();
    }
} 