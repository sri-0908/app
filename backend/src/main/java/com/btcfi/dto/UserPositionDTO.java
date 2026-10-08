package com.btcfi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTO for user position data returned to frontend */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserPositionDTO {
    private Long userId;
    private String walletAddress;
    private String currentPool;
    private Double currentApy;
    private BigDecimal totalEarnings;
    private Double totalDeposited;
    private LocalDateTime lastRebalanceAt;
    private LocalDateTime joinedAt;
    private List<TransactionDTO> recentTransactions;
    private Integer totalRebalances;
}
