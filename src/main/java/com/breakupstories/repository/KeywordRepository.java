package com.breakupstories.repository;

import com.breakupstories.model.Keyword;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordRepository extends MongoRepository<Keyword, String> {
    boolean existsByKey(String key);
    Optional<Keyword> findByKey(String key);
    Page<Keyword> findAll(Pageable pageable);
} 