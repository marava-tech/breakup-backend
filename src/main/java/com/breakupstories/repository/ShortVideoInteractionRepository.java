package com.breakupstories.repository;

import com.breakupstories.model.ShortVideoInteraction;
import com.breakupstories.model.ShortVideoInteraction.InteractionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortVideoInteractionRepository extends MongoRepository<ShortVideoInteraction, String> {

    Optional<ShortVideoInteraction> findByUserIdAndVideoIdAndType(String userId, String videoId, InteractionType type);

    List<ShortVideoInteraction> findByUserIdAndTypeAndCreatedAtAfter(String userId, InteractionType type,
            LocalDateTime after);

    List<ShortVideoInteraction> findByUserIdAndType(String userId, InteractionType type);
}
