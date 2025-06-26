package com.breakupstories.dto;

import com.breakupstories.enums.LANGUAGE;
import com.breakupstories.model.Content;
import com.breakupstories.model.Emotion;
import com.breakupstories.model.Keyword;
import com.breakupstories.model.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {
    
    private String id;
    private String userId;
    private String title;
    private String audioUrl;
    private String shareLink;
    private LANGUAGE audioLanguage;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Story.StoryStatus status;
    private List<Content> contents;
    private List<String> tags;
    private List<Emotion> emotions;
    private List<Keyword> keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLikedByMe = false;
    private boolean isBookmarkedByMe = false;

    public static StoryResponse fromStory(Story story, boolean isLikedByMe, long likeCount, long commentCount) {
        return StoryResponse.builder()
                .id(story.getId())
                .userId(story.getUserId())
                .title(story.getTitle())
                .audioUrl(story.getAudioUrl())
                .shareLink(story.getShareLink())
                .audioLanguage(story.getAudioLanguage())
                .viewCount(story.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .status(story.getStatus())
                .contents(story.getContents())
                .tags(story.getTags())
                .emotions(story.getEmotions())
                .keywords(story.getKeywords())
                .isLikedByMe(isLikedByMe)
                .isBookmarkedByMe(false) // Will be set by service layer
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
    
    public static StoryResponse fromStory(Story story, boolean isLikedByMe, boolean isBookmarkedByMe, long likeCount, long commentCount) {
        return StoryResponse.builder()
                .id(story.getId())
                .userId(story.getUserId())
                .title(story.getTitle())
                .audioUrl(story.getAudioUrl())
                .shareLink(story.getShareLink())
                .audioLanguage(story.getAudioLanguage())
                .viewCount(story.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .status(story.getStatus())
                .contents(story.getContents())
                .tags(story.getTags())
                .emotions(story.getEmotions())
                .keywords(story.getKeywords())
                .isLikedByMe(isLikedByMe)
                .isBookmarkedByMe(isBookmarkedByMe)
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
    
    public static StoryResponse fromStory(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                .userId(story.getUserId())
                .title(story.getTitle())
                .audioUrl(story.getAudioUrl())
                .shareLink(story.getShareLink())
                .audioLanguage(story.getAudioLanguage())
                .viewCount(story.getViewCount())
                .status(story.getStatus())
                .contents(story.getContents())
                .tags(story.getTags())
                .emotions(story.getEmotions())
                .keywords(story.getKeywords())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
} 