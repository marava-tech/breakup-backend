package com.breakupstories.scheduler;

import com.breakupstories.service.AbuseDetectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for running comment abuse analysis
 */
@Component
public class CommentAnalysisScheduler {

    private static final Logger log = LoggerFactory.getLogger(CommentAnalysisScheduler.class);

    private final AbuseDetectionService abuseDetectionService;

    public CommentAnalysisScheduler(AbuseDetectionService abuseDetectionService) {
        this.abuseDetectionService = abuseDetectionService;
    }

    /**
     * Run comment analysis every minute
     */
    @Scheduled(fixedRate = 60000) // every 1 minute
    public void runCommentAnalysis() {
        try {
            log.info("Starting scheduled comment abuse analysis");
            abuseDetectionService.analyzeRecentComments();
            log.info("Completed scheduled comment abuse analysis");
        } catch (Exception e) {
            log.error("Error in scheduled comment analysis: {}", e.getMessage(), e);
        }
    }
}
