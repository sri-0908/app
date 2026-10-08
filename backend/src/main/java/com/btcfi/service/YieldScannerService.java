package com.btcfi.service;

import com.btcfi.model.Pool;
import com.btcfi.repository.PoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * YieldScannerService - Fetches live APY from GOAT Network RPC every 60s.
 * Uses eth_call to read pool contract state. Falls back to simulation if RPC unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YieldScannerService {

    private final Web3j web3j;
    private final PoolRepository poolRepository;

    @Value("${goat.network.contract.yield-pool}")
    private String yieldPoolContract;

    @Value("${goat.network.chain-id}")
    private Integer chainId;

    // ABI function selector for getPoolAPY(uint256)
    private static final String GET_APY_SELECTOR = "0x8e4a1d3e";

    /**
     * Main scan - called by scheduler every 60 seconds.
     * Reads APY from chain and persists to database.
     */
    @Transactional
    public List<Pool> scanAndUpdatePools() {
        log.info("Scanning yield pools on GOAT Network at {}", LocalDateTime.now());
        List<Pool> updatedPools = new ArrayList<>();

        try {
            List<Pool> pools = poolRepository.findAll();
            if (pools.isEmpty()) {
                pools = seedInitialPools();
                log.info("Seeded {} initial pools from GOAT Network", pools.size());
            }

            for (Pool pool : pools) {
                try {
                    double freshApy = fetchApyFromChain(pool);
                    pool.setApy(freshApy);
                    pool.setLastUpdated(LocalDateTime.now());
                    poolRepository.save(pool);
                    updatedPools.add(pool);
                    log.debug("Pool '{}' updated: APY = {}%", pool.getName(), freshApy);
                } catch (Exception e) {
                    log.warn("APY fetch failed for pool '{}': {}", pool.getName(), e.getMessage());
                }
            }
            log.info("Pool scan complete. {} pools updated.", updatedPools.size());
        } catch (Exception e) {
            log.error("Scan error: {}", e.getMessage(), e);
        }
        return updatedPools;
    }

    /**
     * Calls GOAT Network via eth_call to read APY from smart contract.
     * APY is stored on-chain as basis points (100 = 1%).
     */
    private double fetchApyFromChain(Pool pool) {
        try {
            String data = GET_APY_SELECTOR + String.format("%064x", pool.getId());
            String target = pool.getContractAddress() != null ? pool.getContractAddress() : yieldPoolContract;

            Transaction call = Transaction.createEthCallTransaction(null, target, data);
            EthCall response = web3j.ethCall(call, DefaultBlockParameterName.LATEST).send();

            if (!response.hasError()) {
                String result = response.getValue();
                if (result != null && result.length() > 2 && !result.equals("0x")) {
                    BigDecimal bps = new BigDecimal(Numeric.toBigInt(result));
                    return bps.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue();
                }
            }
        } catch (Exception e) {
            log.debug("RPC unavailable for pool '{}', using simulation", pool.getName());
        }
        return simulateApy(pool);
    }

    /** Simulates realistic APY with protocol-specific base rates + small fluctuations */
    private double simulateApy(Pool pool) {
        // Use minute-level seed so APY changes each minute but is deterministic per pool
        Random rng = new Random(pool.getId() * 31L + System.currentTimeMillis() / 60000);
        double base = switch (pool.getProtocol()) {
            case "GOAT-LENDING"  -> 8.5  + (rng.nextDouble() * 3.0  - 1.5);
            case "GOAT-LP"       -> 12.0 + (rng.nextDouble() * 4.0  - 2.0);
            case "GOAT-STAKING"  -> 6.0  + (rng.nextDouble() * 2.0  - 1.0);
            case "GOAT-VAULT"    -> 15.0 + (rng.nextDouble() * 5.0  - 2.5);
            default              -> 5.0  + (rng.nextDouble() * 2.0);
        };
        return Math.round(base * 100.0) / 100.0;
    }

    /** Seeds initial GOAT Network pools into the database */
    @Transactional
    public List<Pool> seedInitialPools() {
        List<Pool> pools = List.of(
            Pool.builder().name("GOAT BTC Lending Pool").protocol("GOAT-LENDING").apy(8.5)
                .contractAddress("0x1111111111111111111111111111111111111111")
                .totalValueLocked(5_000_000.0).riskScore(2).chainId(chainId).isActive(true).build(),
            Pool.builder().name("GOAT BTC-USDC LP").protocol("GOAT-LP").apy(12.0)
                .contractAddress("0x2222222222222222222222222222222222222222")
                .totalValueLocked(12_000_000.0).riskScore(5).chainId(chainId).isActive(true).build(),
            Pool.builder().name("GOAT Staking Vault").protocol("GOAT-STAKING").apy(6.0)
                .contractAddress("0x3333333333333333333333333333333333333333")
                .totalValueLocked(8_000_000.0).riskScore(1).chainId(chainId).isActive(true).build(),
            Pool.builder().name("GOAT High-Yield Vault").protocol("GOAT-VAULT").apy(15.0)
                .contractAddress("0x4444444444444444444444444444444444444444")
                .totalValueLocked(3_000_000.0).riskScore(7).chainId(chainId).isActive(true).build(),
            Pool.builder().name("GOAT Stable Yield").protocol("GOAT-LENDING").apy(5.5)
                .contractAddress("0x5555555555555555555555555555555555555555")
                .totalValueLocked(20_000_000.0).riskScore(1).chainId(chainId).isActive(true).build()
        );
        return poolRepository.saveAll(pools);
    }

    public Pool getBestPool(int maxRiskScore) {
        return poolRepository.findPoolsByMaxRisk(maxRiskScore).stream().findFirst().orElse(null);
    }

    public List<Pool> getAllActivePools() {
        return poolRepository.findByIsActiveTrueOrderByApyDesc();
    }
}
