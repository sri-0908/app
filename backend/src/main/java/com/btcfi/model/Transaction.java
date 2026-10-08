package com.btcfi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Transaction entity - records every rebalance action */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "transactions")
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "from_pool")
    private String fromPool;

    @Column(name = "to_pool", nullable = false)
    private String toPool;

    @Column(name = "amount", precision = 18, scale = 8)
    private BigDecimal amount;

    @Column(name = "fee", precision = 18, scale = 8)
    private BigDecimal fee;

    @Column(name = "tx_hash", length = 66)
    private String txHash;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "apy_before")
    private Double apyBefore;

    @Column(name = "apy_after")
    private Double apyAfter;

    @Column(name = "gas_used")
    private Long gasUsed;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    protected void onCreate() { timestamp = LocalDateTime.now(); }
}
