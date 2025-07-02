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
public class ReferralStatsResponse {
    private String referralCode;
    private String referredBy;
    private int referredUsersCount;
    private List<String> referredUsers;
} 