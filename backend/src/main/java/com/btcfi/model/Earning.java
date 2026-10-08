package com.btcfi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Earning entity - tracks yield earnings per user per period */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "earnings")
public class Earning {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 8)
    private BigDecimal amount;

    /** Period format: "2024-01" */
    @Column(name = "period", nullable = false, length = 20)
    private String period;

    @Column(name = "pool_id")
    private Long poolId;

    @Column(name = "pool_name")
    private String poolName;

    @Column(name = "apy_at_record")
    private Double apyAtRecord;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() { recordedAt = LocalDateTime.now(); }
}
