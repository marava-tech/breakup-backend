package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardConfigResponse {
    
    private Map<String, String> rewardConfigs;
    private Map<String, String> referralConfigs;
    
    public static RewardConfigResponse fromConfigMaps(Map<String, String> rewardConfigs, Map<String, String> referralConfigs) {
        return RewardConfigResponse.builder()
                .rewardConfigs(rewardConfigs)
                .referralConfigs(referralConfigs)
                .build();
    }
} 