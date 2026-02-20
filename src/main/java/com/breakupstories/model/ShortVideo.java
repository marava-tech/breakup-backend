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
@Document(collection = "short_videos")
@CompoundIndexes({
        @CompoundIndex(name = "idx_video_status_date", def = "{'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_video_status_views", def = "{'status': 1, 'viewCount': -1}")
})
public class ShortVideo {
    @Id
    private String id;

    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;

    @Indexed
    private String language;
    @Indexed
    private List<String> tags;

    @Builder.Default
    @Indexed(direction = IndexDirection.DESCENDING)
    private Long viewCount = 0L;

    @Builder.Default
    private Long likeCount = 0L;

    @Builder.Default
    private Long commentCount = 0L;
    @Builder.Default
    private Long shareCount = 0L;

    @Indexed
    private VideoStatus status;

    @CreatedDate
    @Indexed(direction = IndexDirection.DESCENDING)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum VideoStatus {
        ACTIVE, INACTIVE
    }
}
