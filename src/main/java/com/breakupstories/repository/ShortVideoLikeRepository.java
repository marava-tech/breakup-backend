package com.breakupstories.repository;

import com.breakupstories.model.ShortVideoLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortVideoLikeRepository extends MongoRepository<ShortVideoLike, String> {

    Optional<ShortVideoLike> findByUserIdAndVideoId(String userId, String videoId);

    boolean existsByUserIdAndVideoId(String userId, String videoId);
}
