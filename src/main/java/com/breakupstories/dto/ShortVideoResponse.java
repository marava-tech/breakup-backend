package com.breakupstories.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.breakupstories.model.ShortVideo.VideoStatus;

@Data
@Builder
public class ShortVideoResponse {
    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String language;
    private List<String> tags;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private VideoStatus status;
    private LocalDateTime createdAt;

    // User interactions flag
    private boolean isLiked;
}
