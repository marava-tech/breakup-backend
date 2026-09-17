package com.breakupstories.repository;

import com.breakupstories.model.NoContactProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoContactProfileRepository extends MongoRepository<NoContactProfileDocument, String> {
    Optional<NoContactProfileDocument> findByUserId(String userId);
}
