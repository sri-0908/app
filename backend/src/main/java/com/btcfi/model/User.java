package com.btcfi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** User entity - wallet owner using the BTCFi yield optimizer */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_address", unique = true, nullable = false, length = 42)
    private String walletAddress;

    @Column(name = "current_pool")
    private String currentPool;

    @Column(name = "current_pool_id")
    private Long currentPoolId;

    @Column(name = "total_deposited")
    private Double totalDeposited;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_rebalance_at")
    private LocalDateTime lastRebalanceAt;

    @PrePersist
    protected void onCreate() { joinedAt = LocalDateTime.now(); }
}
