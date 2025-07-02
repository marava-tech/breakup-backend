package com.breakupstories.repository;

import com.breakupstories.model.CoinHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinHistoryRepository extends MongoRepository<CoinHistory, String> {
    
    /**
     * Find all coin history entries for a user
     */
    Page<CoinHistory> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find all coin history entries for a user
     */
    List<CoinHistory> findByUserId(String userId);
    
    /**
     * Find coin history entries by reason for a user
     */
    List<CoinHistory> findByUserIdAndReason(String userId, String reason);
    
    /**
     * Find coin history entries by related entity ID
     */
    List<CoinHistory> findByRelatedEntityId(String relatedEntityId);
    
    /**
     * Find coin history entries by user and related entity ID
     */
    List<CoinHistory> findByUserIdAndRelatedEntityId(String userId, String relatedEntityId);
    
    /**
     * Check if a user has already received a reward for a specific reason and entity
     */
    boolean existsByUserIdAndReasonAndRelatedEntityId(String userId, String reason, String relatedEntityId);
    
    /**
     * Check if a user has already received a reward for a specific reason
     */
    boolean existsByUserIdAndReason(String userId, String reason);
    
    /**
     * Get total coins earned by a user
     */
    @Query(value = "{'userId': ?0}", fields = "{'count': 1}")
    List<CoinHistory> findCountsByUserId(String userId);
} 