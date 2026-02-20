package com.breakupstories.dto;

import com.breakupstories.model.User;
import com.breakupstories.model.ShortVideoComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortVideoCommentResponse {
    private String id;
    private String videoId;
    private String userId;
    private String username;
    private String userProfilePicture;
    private String text;
    private String parentId;
    private LocalDateTime createdAt;

    public static ShortVideoCommentResponse fromCommentAndUser(ShortVideoComment comment, User user) {
        return ShortVideoCommentResponse.builder()
                .id(comment.getId())
                .videoId(comment.getVideoId())
                .userId(comment.getUserId())
                .username(user != null ? user.getName() : "Unknown")
                .userProfilePicture(user != null ? user.getProfileImageUrl() : null)
                .text(comment.getText())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
