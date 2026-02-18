package com.breakupstories.controller;

import com.breakupstories.dto.AddCoinHistoryRequest;
import com.breakupstories.dto.CoinHistoryInvalidationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rewards", description = "Reward and referral management APIs (Deprecated)")
@Deprecated
public class RewardController {

    @GetMapping("/coins")
    @Operation(summary = "Get coin balance (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getCoinBalance(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.GONE).body("Coins feature is currently disabled.");
    }

    @GetMapping("/referral-stats")
    @Operation(summary = "Get referral statistics (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getReferralStats(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.GONE).body("Referral feature is currently disabled.");
    }

    @GetMapping("/coins/{userId}")
    @Operation(summary = "Get coin balance by user ID (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getCoinBalanceByUserId(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.GONE).body("Coins feature is currently disabled.");
    }

    @PutMapping("/coin-history/invalidate")
    @Operation(summary = "Invalidate coin history (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> invalidateCoinHistory(@Valid @RequestBody CoinHistoryInvalidationRequest request) {
        return ResponseEntity.status(HttpStatus.GONE).body("Coins feature is currently disabled.");
    }

    @PostMapping("/coin-history")
    @Operation(summary = "Add coin history (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> addCoinHistory(@Valid @RequestBody AddCoinHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.GONE).body("Coins feature is currently disabled.");
    }
}