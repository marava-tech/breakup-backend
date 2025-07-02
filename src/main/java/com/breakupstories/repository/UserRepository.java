package com.breakupstories.repository;

import com.breakupstories.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    /**
     * Find user by referral code
     */
    Optional<User> findByReferralCode(String referralCode);
    
    /**
     * Check if referral code exists
     */
    boolean existsByReferralCode(String referralCode);
    
    /**
     * Find users referred by a specific user
     */
    List<User> findByReferredBy(String referredBy);
} 