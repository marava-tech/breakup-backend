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
public class NoContactSyncRequest {
    private String startDate; // ISO 8601 string
    private int targetDays;
    private String exNickname;
    private String motivation;
    private String lastCheckInDate;
    private int sosCount;
    private List<SyncMoodCheckIn> checkIns;
    private List<SyncResistedMessage> resistedMessages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncMoodCheckIn {
        private String date;
        private String mood;
        private int urgeLevel;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResistedMessage {
        private String id;
        private String timestamp;
        private String content;
        private int urgeLevel;
        private boolean burned;
    }
}
