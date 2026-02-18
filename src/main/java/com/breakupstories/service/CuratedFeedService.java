package com.breakupstories.service;

import com.breakupstories.model.Story;
import com.breakupstories.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuratedFeedService {

    private static final String CURATED_IDS_KEY = "feed:curated:ids";
    private static final int CURATED_LIMIT = 50;
    private static final Duration TTL = Duration.ofDays(1);

    private final StoryRepository storyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendationService recommendationService;

    /**
     * Nightly refresh of curated feed.
     * Runs at 3 AM every day.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshCuratedFeed() {
        log.info("Starting nightly curated feed refresh...");
        try {
            // Curated = High completion rate + Recent (last 7 days)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            List<Story> candidates = storyRepository.findByStatus(Story.StoryStatus.ACTIVE);

            List<String> curatedIds = candidates.stream()
                    .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(sevenDaysAgo))
                    .sorted((s1, s2) -> Double.compare(
                            calculateCuratedScore(s2),
                            calculateCuratedScore(s1)))
                    .limit(CURATED_LIMIT)
                    .map(Story::getId)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(CURATED_IDS_KEY, curatedIds, TTL);
            log.info("Refreshed curated feed with {} stories", curatedIds.size());
        } catch (Exception e) {
            log.error("Failed to refresh curated feed: {}", e.getMessage(), e);
        }
    }

    private double calculateCuratedScore(Story s) {
        // For curation, we don't have a specific user context, so we use global
        // recommendation logic
        return recommendationService.calculateAffinityScore(null, s);
    }

    @SuppressWarnings("unchecked")
    public List<String> getCuratedStoryIds() {
        try {
            Object cached = redisTemplate.opsForValue().get(CURATED_IDS_KEY);
            if (cached instanceof List) {
                return (List<String>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to read curated feed from cache: {}", e.getMessage());
        }

        // If cache miss, return empty or trigger a quick refresh?
        // For now, return empty and let the caller handle it.
        return List.of();
    }
}
