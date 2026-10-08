package com.btcfi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Pool entity - a yield-generating pool on GOAT Network */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "pools")
public class Pool {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "apy", nullable = false)
    private Double apy;

    @Column(name = "protocol", nullable = false, length = 50)
    private String protocol;

    @Column(name = "contract_address", length = 42)
    private String contractAddress;

    @Column(name = "total_value_locked")
    private Double totalValueLocked;

    /** Risk score 1-10: lower = safer */
    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "chain_id")
    private Integer chainId;

    @PreUpdate @PrePersist
    protected void onUpdate() { lastUpdated = LocalDateTime.now(); }
}
