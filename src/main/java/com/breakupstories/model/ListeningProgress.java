package com.breakupstories.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "listening_progress")
@CompoundIndexes({
        @CompoundIndex(name = "user_story_idx", def = "{'userId': 1, 'storyId': 1}", unique = true),
        @CompoundIndex(name = "user_updated_idx", def = "{'userId': 1, 'updatedAt': -1}")
})
public class ListeningProgress {

    @Id
    private String id;

    private String userId;
    private String storyId;
    private Double progressSeconds;
    private boolean completed;
    private Boolean playCounted;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
