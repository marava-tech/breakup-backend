package com.breakupstories.dto;

import com.breakupstories.model.CoinHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinBalanceResponse {
    private int totalCoins;
    private List<CoinHistory> coinHistory;
} 