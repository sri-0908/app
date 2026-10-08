package com.btcfi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** DTO for manual rebalance API requests */
@Data
public class RebalanceRequestDTO {

    @NotBlank(message = "Wallet address is required")
    private String walletAddress;

    private Long targetPoolId;

    private Boolean forceRebalance = false;
}
