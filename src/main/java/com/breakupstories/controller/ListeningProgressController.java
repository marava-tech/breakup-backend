package com.breakupstories.controller;

import com.breakupstories.dto.ListeningProgressRequest;
import com.breakupstories.service.ListeningProgressService;
import com.breakupstories.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Listening Progress", description = "API for tracking listening progress")
public class ListeningProgressController {

    private final ListeningProgressService listeningProgressService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Update listening progress", description = "Update progress for a story. Marks as completed if progress > 90% or explicit flag.")
    public ResponseEntity<?> updateProgress(
            Authentication authentication,
            @Valid @RequestBody ListeningProgressRequest request) {

        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();

        listeningProgressService.updateProgress(userId, request);

        return ResponseEntity.ok(Map.of("success", true, "message", "Progress updated"));
    }
}
