package com.btcfi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for transaction history */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String fromPool;
    private String toPool;
    private BigDecimal amount;
    private BigDecimal fee;
    private String txHash;
    private String status;
    private Double apyBefore;
    private Double apyAfter;
    private LocalDateTime timestamp;
}
