package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosRealityCheckRequest {
    private String message;
    private int daysClean;
    private String exNickname;
    private int urgeLevel;
}
