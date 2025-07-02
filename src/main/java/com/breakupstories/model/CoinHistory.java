package com.breakupstories.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "coin_history")
public class CoinHistory {
    
    @Id
    private String id;
    
    private String userId;
    private int count;
    private String reason;
    private String relatedEntityId; // Optional: for story-based rewards, like storyId
    
    @CreatedDate
    private Long createdAt;
} 