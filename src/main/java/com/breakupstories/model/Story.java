package com.breakupstories.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stories")
@CompoundIndexes({
        @CompoundIndex(name = "idx_stories_lang_status_date", def = "{'language': 1, 'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_status_views", def = "{'status': 1, 'viewCount': -1}"),
        @CompoundIndex(name = "idx_stories_status_plays", def = "{'status': 1, 'playCount': -1}"),
        @CompoundIndex(name = "idx_stories_status_date", def = "{'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_user_status_date", def = "{'userId': 1, 'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_status_category_date", def = "{'status': 1, 'category': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_status_lang_category_date", def = "{'status': 1, 'language': 1, 'category': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_creation_status_date", def = "{'creationType': 1, 'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_creation_status_lang_date", def = "{'creationType': 1, 'status': 1, 'language': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_stories_status_tags", def = "{'status': 1, 'tags': 1}"),
        @CompoundIndex(name = "idx_stories_status_lang_tags", def = "{'status': 1, 'language': 1, 'tags': 1}"),
        @CompoundIndex(name = "idx_stories_status_completion_play", def = "{'status': 1, 'completionCount': -1, 'playCount': -1}"),
        @CompoundIndex(name = "idx_stories_lang_status_completion_play", def = "{'language': 1, 'status': 1, 'completionCount': -1, 'playCount': -1}")
})
public class Story {

    @Id
    private String id;

    @Indexed
    private String userId;
    private String title;
    private String audioUrl;
    @Deprecated
    private String thumbnailUrl;
    private String coverImageUrl;
    private String author;
    private List<String> storyImages;
    @Builder.Default
    @Indexed(direction = IndexDirection.DESCENDING)
    private Long viewCount = 0L;
    @Builder.Default
    @Indexed(direction = IndexDirection.DESCENDING)
    private Long playCount = 0L;
    @Builder.Default
    @Indexed(direction = IndexDirection.DESCENDING)
    private Long completionCount = 0L;

    private String spotifyUrl;

    private Long duration; // Duration in milliseconds
    @Indexed
    private StoryStatus status;

    @Deprecated
    private List<Content> contents;
    @Indexed
    private List<String> tags;
    @Deprecated
    private List<Emotion> emotions;

    @Indexed
    private Category category;

    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private List<String> rejectionReasons;

    @Indexed
    private String language;

    @Indexed
    private CreationType creationType;

    public enum StoryStatus {
        UPLOAD_PENDING, UPLOADING, PROCESSING_PENDING, PROCESSING, PROCESSED, CONVERTING, ACTIVE, INACTIVE, FAILED,
        REJECTED
    }

    public enum CreationType {
        UPLOADED, WRITTEN
    }

    public enum Category {
        FRESH_BREAKUP, TOXIC, HEALING, LATE_NIGHT, STRONG, NUMB
    }
}