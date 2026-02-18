package com.breakupstories.repository;

import com.breakupstories.model.ListeningProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListeningProgressRepository extends MongoRepository<ListeningProgress, String> {

    Optional<ListeningProgress> findByUserIdAndStoryId(String userId, String storyId);

    Page<ListeningProgress> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    List<ListeningProgress> findByUserIdAndUpdatedAtAfter(String userId, LocalDateTime date);
}
