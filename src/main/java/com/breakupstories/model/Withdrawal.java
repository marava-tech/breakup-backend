package com.breakupstories.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "withdrawals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_user_status_created", def = "{'userId': 1, 'status': 1, 'createdAt': -1}")
})
public class Withdrawal {

    @Id
    private String id;

    private String userId;
    private Integer coins;
    private BigDecimal moneyInRs;
    private String upiId;
    private WithdrawalStatus status;
    private String withdrawalProofImageUrl;
    private String comments; // Admin comments for withdrawal status updates
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum WithdrawalStatus {
        PENDING,
        PROCESSING,
        PROCESSED,
        REJECTED
    }
}