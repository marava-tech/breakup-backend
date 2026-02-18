package com.breakupstories.controller;

import com.breakupstories.dto.WithdrawalRequest;
import com.breakupstories.model.Withdrawal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Withdrawals", description = "Withdrawal management APIs (Deprecated)")
@Deprecated
public class WithdrawalController {

    @PostMapping
    @Operation(summary = "Create withdrawal request (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> createWithdrawal(
            Authentication authentication,
            @Valid @RequestBody WithdrawalRequest request) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

    @PutMapping("/{withdrawalId}/status")
    @Operation(summary = "Update withdrawal status (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> updateWithdrawalStatus(
            @PathVariable String withdrawalId,
            MultipartHttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

    @GetMapping("/{withdrawalId}")
    @Operation(summary = "Get withdrawal by ID (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getWithdrawalById(@PathVariable String withdrawalId) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

    @GetMapping("/options")
    @Operation(summary = "Get withdrawal options (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getWithdrawalOptions(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

    @GetMapping("/my-withdrawals")
    @Operation(summary = "Get my withdrawals (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getWithdrawals(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

    // Admin endpoints
    @GetMapping
    @Operation(summary = "Get all withdrawals (Disabled)", description = "Feature disabled")
    public ResponseEntity<?> getAllWithdrawals(
            @RequestParam(required = false) Withdrawal.WithdrawalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.status(HttpStatus.GONE).body("Withdrawals feature is currently disabled.");
    }

}