package com.breakupstories.service;

import com.breakupstories.dto.ListeningProgressRequest;
import com.breakupstories.dto.StoryResponse;
import com.breakupstories.model.ListeningProgress;
import com.breakupstories.repository.ListeningProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListeningProgressService {

    private final ListeningProgressRepository listeningProgressRepository;
    private final StoryService storyService;
    private final RecommendationService recommendationService;

    public void updateProgress(String userId, ListeningProgressRequest request) {
        // Upsert progress
        ListeningProgress progress = listeningProgressRepository.findByUserIdAndStoryId(userId, request.getStoryId())
                .orElseGet(() -> {
                    // This is likely the first time the user is viewing this story
                    recommendationService.trackInteraction(userId, request.getStoryId(),
                            RecommendationService.InteractionType.VIEW);
                    return ListeningProgress.builder()
                            .userId(userId)
                            .storyId(request.getStoryId())
                            .playCounted(false)
                            .build();
                });

        progress.setProgressSeconds(request.getProgressSeconds());

        // Increment playCount after 10 seconds (P0 Task)
        if (request.getProgressSeconds() >= 10 && (progress.getPlayCounted() == null || !progress.getPlayCounted())) {
            storyService.incrementPlayCountAsync(request.getStoryId());
            progress.setPlayCounted(true);
            log.info("Play count incremented for story {} by user {}", request.getStoryId(), userId);
        }

        // Determine completion based on 90% threshold (P0 Task)
        boolean isCurrentlyCompleted = false;
        if (request.getCompleted() != null && request.getCompleted()) {
            isCurrentlyCompleted = true;
        } else if (request.getTotalDurationSeconds() != null && request.getTotalDurationSeconds() > 0) {
            // Mark complete after 90%
            if (request.getProgressSeconds() >= (request.getTotalDurationSeconds() * 0.9)) {
                isCurrentlyCompleted = true;
            }
        }

        // Once completed, preserve that status
        if (progress.isCompleted() || isCurrentlyCompleted) {
            if (!progress.isCompleted()) {
                storyService.incrementCompletionCountAsync(request.getStoryId());
                recommendationService.trackInteraction(userId, request.getStoryId(),
                        RecommendationService.InteractionType.COMPLETE);
                log.info("Completion count incremented for story {} by user {}", request.getStoryId(), userId);
            }
            progress.setCompleted(true);
        }

        progress.setUpdatedAt(LocalDateTime.now());
        listeningProgressRepository.save(progress);
    }

    public Page<ListeningProgress> getResumeValue(String userId, int page, int size) {
        return listeningProgressRepository.findByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(page, size));
    }

    public List<StoryResponse> getResumeStories(String userId, int page, int size) {
        Page<ListeningProgress> progressPage = listeningProgressRepository.findByUserIdOrderByUpdatedAtDesc(
                userId, PageRequest.of(page, size));

        return progressPage.getContent().stream()
                .filter(p -> !p.isCompleted()) // Only resume incomplete stories? Or all? PRD says "Resume". usually
                                               // implies incomplete.
                // Actually PRD doesn't explicitly say "only incomplete", but "resume" implies
                // it.
                // let's include all recently listened but maybe filter in UI?
                // Standard resume is usually for things in progress.
                // Let's filter for now.
                .filter(p -> p.getProgressSeconds() > 0)
                .map(p -> storyService.getStoryById(p.getStoryId(), userId)) // Fetch story details
                .collect(Collectors.toList());
    }
}
