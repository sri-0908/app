package com.btcfi.controller;

import com.btcfi.dto.PoolDTO;
import com.btcfi.model.Pool;
import com.btcfi.service.YieldScannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pools")
@RequiredArgsConstructor
public class PoolController {

    private final YieldScannerService yieldScannerService;

    @GetMapping
    public ResponseEntity<List<PoolDTO>> getAllPools() {
        List<Pool> pools = yieldScannerService.getAllActivePools();
        Pool bestPool = yieldScannerService.getBestPool(7);
        
        List<PoolDTO> poolDTOs = pools.stream().map(p -> PoolDTO.builder()
            .id(p.getId())
            .name(p.getName())
            .apy(p.getApy())
            .protocol(p.getProtocol())
            .contractAddress(p.getContractAddress())
            .totalValueLocked(p.getTotalValueLocked())
            .riskScore(p.getRiskScore())
            .riskLabel(p.getRiskScore() <= 3 ? "LOW" : (p.getRiskScore() <= 7 ? "MEDIUM" : "HIGH"))
            .lastUpdated(p.getLastUpdated())
            .isActive(p.getIsActive())
            .isBestPool(bestPool != null && bestPool.getId().equals(p.getId()))
            .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(poolDTOs);
    }
}
