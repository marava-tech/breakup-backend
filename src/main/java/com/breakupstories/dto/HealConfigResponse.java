package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealConfigResponse {
    private boolean success;
    private List<MilestoneConfig> milestones;
    private List<DailyAffirmationConfig> dailyAffirmations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneConfig {
        private int days;
        private String title;
        private String stage;
        private String badgeIcon;
        private String psychologicalFact;
        private String affirmation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAffirmationConfig {
        private int day;
        private String affirmation;
        private String fact;
    }
}
