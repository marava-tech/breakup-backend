package com.breakupstories.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private String quoteText;
    private String quoteAudio;
    private String quoteImage;
    private int textNumber;
    private int audioNumber;
    private int imageNumber;
}
