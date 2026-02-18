package com.breakupstories.scheduler;

import com.breakupstories.model.StoryDataStore;
import com.breakupstories.service.StoryProcessingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
public class StoryProcessingScheduler {

    private static final Logger log = LoggerFactory.getLogger(StoryProcessingScheduler.class);

    private final StoryProcessingService storyProcessingService;

    public StoryProcessingScheduler(StoryProcessingService storyProcessingService) {
        this.storyProcessingService = storyProcessingService;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void processPendingStories() {
        log.info("Starting scheduled story processing - fetching pending stories");

        // Fetch up to 10 pending stories and mark them as PROCESSING atomically
        List<StoryDataStore> storiesToProcess = storyProcessingService.fetchAndMarkProcessingStories();

        if (storiesToProcess.isEmpty()) {
            log.info("No stories to process");
            return;
        }

        log.info("Found {} stories to process", storiesToProcess.size());

        // Log storyId of each fetched story
        for (StoryDataStore story : storiesToProcess) {
            log.info("Processing story with storyId: {}", story.getStoryId());
        }

        // Process each story through the AI pipeline
        for (StoryDataStore story : storiesToProcess) {
            try {
                storyProcessingService.processStory(story);
            } catch (Exception e) {
                log.error("Error processing story with storyId: {}", story.getStoryId(), e);
            }
        }

        log.info("Completed scheduled story processing");
    }
}
