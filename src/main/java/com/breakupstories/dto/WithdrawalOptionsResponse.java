package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalOptionsResponse {
    
    private List<WithdrawalOptionResponse> options;
    private String defaultProcessingTime;
    private boolean pauseWithdrawals;
    private String pauseWithdrawalsReason;
    
    public static WithdrawalOptionsResponse of(List<WithdrawalOptionResponse> options, String defaultProcessingTime, boolean pauseWithdrawals, String pauseWithdrawalsReason) {
        return WithdrawalOptionsResponse.builder()
                .options(options)
                .defaultProcessingTime(defaultProcessingTime)
                .pauseWithdrawals(pauseWithdrawals)
                .pauseWithdrawalsReason(pauseWithdrawalsReason)
                .build();
    }
} 