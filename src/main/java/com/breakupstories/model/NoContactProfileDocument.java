package com.breakupstories.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "no_contact_profiles")
public class NoContactProfileDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private LocalDateTime startDate;
    private int targetDays;
    private String exNickname;
    private String motivation;
    private LocalDateTime lastCheckInDate;
    private int sosCount;

    @Builder.Default
    private List<MoodCheckInEntry> checkIns = new ArrayList<>();

    @Builder.Default
    private List<ResistedMessageEntry> resistedMessages = new ArrayList<>();

    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoodCheckInEntry {
        private LocalDateTime date;
        private String mood;
        private int urgeLevel;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResistedMessageEntry {
        private String id;
        private LocalDateTime timestamp;
        private String content;
        private int urgeLevel;
        private boolean burned;
    }
}
