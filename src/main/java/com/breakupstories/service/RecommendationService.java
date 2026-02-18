package com.breakupstories.service;

import com.breakupstories.model.Story;
import com.breakupstories.model.User;
import com.breakupstories.repository.StoryRepository;
import com.breakupstories.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    public enum InteractionType {
        VIEW(0.1),
        LIKE(1.0),
        COMPLETE(2.0),
        BOOKMARK(0.5);

        private final double weight;

        InteractionType(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }

    @Async("storyOpsExecutor")
    public void trackInteraction(String userId, String storyId, InteractionType type) {
        log.info("Tracking interaction: user={}, story={}, type={}", userId, storyId, type);

        storyRepository.findById(storyId).ifPresent(story -> {
            userRepository.findById(userId).ifPresent(user -> {
                updatePreferences(user, story, type);
                userRepository.save(user);
            });
        });
    }

    private void updatePreferences(User user, Story story, InteractionType type) {
        // Update category preferences
        if (story.getCategory() != null) {
            Map<String, Double> catPrefs = user.getCategoryPreferences();
            if (catPrefs == null)
                catPrefs = new HashMap<>();

            String cat = story.getCategory().name();
            catPrefs.put(cat, catPrefs.getOrDefault(cat, 0.0) + type.getWeight());
            user.setCategoryPreferences(catPrefs);
        }

        // Update language preferences
        if (story.getLanguage() != null) {
            Map<String, Double> langPrefs = user.getLanguagePreferences();
            if (langPrefs == null)
                langPrefs = new HashMap<>();

            String lang = story.getLanguage();
            langPrefs.put(lang, langPrefs.getOrDefault(lang, 0.0) + type.getWeight());
            user.setLanguagePreferences(langPrefs);
        }
    }

    public double calculateAffinityScore(User user, Story story) {
        double score = 0.0;

        // Base score metrics (Completion rate + Play count)
        long plays = story.getPlayCount() != null ? story.getPlayCount() : 0;
        long completions = story.getCompletionCount() != null ? story.getCompletionCount() : 0;
        double completionRate = (plays > 0) ? (double) completions / plays : 0.0;

        score += completionRate * 10.0; // Highly weight stories that others finish
        score += plays * 0.001; // Small boost for high play count

        if (user == null)
            return score;

        // Personalization: Category match
        if (story.getCategory() != null && user.getCategoryPreferences() != null) {
            score += user.getCategoryPreferences().getOrDefault(story.getCategory().name(), 0.0) * 2.0;
        }

        // Personalization: Language match
        if (story.getLanguage() != null && user.getLanguagePreferences() != null) {
            score += user.getLanguagePreferences().getOrDefault(story.getLanguage(), 0.0) * 5.0;
        }

        // Boost stories in user's preferred language
        if (story.getLanguage() != null && story.getLanguage().equalsIgnoreCase(user.getPreferredStoryLanguage())) {
            score += 20.0;
        }

        return score;
    }
}
