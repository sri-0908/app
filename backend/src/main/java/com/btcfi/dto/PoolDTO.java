package com.btcfi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** DTO for pool data returned to frontend */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PoolDTO {
    private Long id;
    private String name;
    private Double apy;
    private String protocol;
    private String contractAddress;
    private Double totalValueLocked;
    private Integer riskScore;
    private String riskLabel;    // LOW, MEDIUM, HIGH
    private LocalDateTime lastUpdated;
    private Boolean isActive;
    private Boolean isBestPool;
}
