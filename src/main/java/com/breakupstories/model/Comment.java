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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
@CompoundIndexes({
    @CompoundIndex(name = "idx_story_active_created", def = "{'storyId': 1, 'active': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_user_active_created", def = "{'userId': 1, 'active': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_abusive_active", def = "{'isAbusive': 1, 'active': 1}"),
    @CompoundIndex(name = "idx_created_category_explanation", def = "{'createdAt': 1, 'category': 1, 'explanation': 1, 'confidence': 1}")
})
public class Comment {
    
    @Id
    private String id;

    @Indexed
    private String storyId;

    @Indexed
    private String userId;

    private String parentId; // nullable for replies
    private String text;

    @Indexed
    private boolean active = true; // default to true for new comments

    // Abuse detection fields
    @Builder.Default
    @Indexed
    private boolean isAbusive = false; // default to false for new comments

    private Double confidence;
    private String category; // category of abuse if detected
    private String explanation; // explanation of why comment was flagged as abusive
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
} 