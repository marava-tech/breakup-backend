package com.breakupstories.service;

import com.breakupstories.dto.PagedResponse;
import com.breakupstories.dto.ShortVideoResponse;
import com.breakupstories.model.ShortVideo;
import com.breakupstories.model.ShortVideoInteraction;
import com.breakupstories.repository.ShortVideoInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.breakupstories.util.LanguageUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortVideoRecommendationService {

    private final ShortVideoInteractionRepository interactionRepository;
    private final ShortVideoService shortVideoService;
    private final MongoTemplate mongoTemplate;

    public PagedResponse<ShortVideoResponse> getFeed(String userId, String language, int size) {
        List<String> watchedIds = Collections.emptyList();
        if (userId != null) {
            // Fetch recent watched IDs to avoid huge queries, taking the last 2000 views
            watchedIds = interactionRepository.findByUserIdAndType(userId, ShortVideoInteraction.InteractionType.VIEW)
                    .stream()
                    .sorted(Comparator.comparing(ShortVideoInteraction::getCreatedAt).reversed())
                    .limit(2000)
                    .map(ShortVideoInteraction::getVideoId)
                    .collect(Collectors.toList());
            log.info("User {} has watched {} videos", userId, watchedIds.size());
        }

        Criteria criteria = Criteria.where("status").is(ShortVideo.VideoStatus.ACTIVE);

        if (!watchedIds.isEmpty()) {
            criteria = criteria.and("_id").nin(watchedIds);
        }

        if (language != null && !language.trim().isEmpty()) {
            List<String> variants = LanguageUtils.getLanguageVariants(language);
            log.info("Filtering short videos by language variants: {}", variants);
            criteria = criteria.and("language").in(variants);
        }

        log.info("Short video feed criteria: {}", criteria.getCriteriaObject());

        MatchOperation matchStage = Aggregation.match(criteria);
        SampleOperation sampleStage = Aggregation.sample(size);

        Aggregation aggregation = Aggregation.newAggregation(matchStage, sampleStage);

        AggregationResults<ShortVideo> results = mongoTemplate.aggregate(aggregation, ShortVideo.class,
                ShortVideo.class);

        List<ShortVideo> mappedResults = results.getMappedResults();

        // Fallback: if no unseen videos found and we were filtering watched ones, relax
        // the criteria
        if (mappedResults.isEmpty() && !watchedIds.isEmpty()) {
            log.info("No unseen videos found for user {}, falling back to all active videos", userId);
            Criteria fallbackCriteria = Criteria.where("status").is(ShortVideo.VideoStatus.ACTIVE);
            if (language != null && !language.trim().isEmpty()) {
                fallbackCriteria = fallbackCriteria.and("language").in(LanguageUtils.getLanguageVariants(language));
            }
            aggregation = Aggregation.newAggregation(Aggregation.match(fallbackCriteria), sampleStage);
            results = mongoTemplate.aggregate(aggregation, ShortVideo.class, ShortVideo.class);
            mappedResults = results.getMappedResults();
        }

        log.info("Short video feed found {} results", mappedResults.size());

        List<ShortVideoResponse> responses = mappedResults.stream()
                .map(v -> shortVideoService.mapToResponse(v, userId))
                .collect(Collectors.toList());

        return PagedResponse.of(responses, 0, size, 10000L);
    }
}
