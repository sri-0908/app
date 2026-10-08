package com.btcfi.yieldoptimizer.service;

import com.btcfi.yieldoptimizer.config.Web3jConfig.Web3jProperties;
import com.btcfi.yieldoptimizer.model.Pool;
import com.btcfi.yieldoptimizer.repository.PoolRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@EnableScheduling
public class YieldScannerService {

    private static final Logger log = LoggerFactory.getLogger(YieldScannerService.class);

    private final PoolRepository poolRepository;
    private final Web3j web3j;
    private final Web3jProperties web3jProperties;
    private final Random random = new Random();

    // Map of pool ID to mock target contract addresses for live mode scanning
    private final Map<String, String> poolContracts = new HashMap<>();

    @Autowired
    public YieldScannerService(PoolRepository poolRepository, Web3j web3j, Web3jProperties web3jProperties) {
        this.poolRepository = poolRepository;
        this.web3j = web3j;
        this.web3jProperties = web3jProperties;

        // Initialize contract addresses for the pools
        poolContracts.put("lorenzo-btc", "0x5555444433332222111100009999888877776666");
        poolContracts.put("babylon-btc", "0x6666555544443333222211110000999988887777");
        poolContracts.put("pell-btc", "0x7777666655554444333322221111000099998888");
    }

    @PostConstruct
    public void setupInitialPools() {
        if (poolRepository.count() == 0) {
            log.info("Initializing default BTCFi yield pools in the database.");
            
            Pool lorenzo = Pool.builder()
                    .id("lorenzo-btc")
                    .name("Lorenzo Staked BTC")
                    .protocol("Lorenzo Protocol")
                    .apy(new BigDecimal("8.50"))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            Pool babylon = Pool.builder()
                    .id("babylon-btc")
                    .name("Babylon Direct Staking")
                    .protocol("Babylon Protocol")
                    .apy(new BigDecimal("7.20"))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            Pool pell = Pool.builder()
                    .id("pell-btc")
                    .name("Pell Network Restaking")
                    .protocol("Pell Network")
                    .apy(new BigDecimal("9.10"))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            poolRepository.save(lorenzo);
            poolRepository.save(babylon);
            poolRepository.save(pell);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void scanYieldPools() {
        log.info("Starting scheduled yield scan on GOAT Network RPC...");

        List<Pool> pools = poolRepository.findAll();
        for (Pool pool : pools) {
            BigDecimal apy = fetchApyFromChain(pool.getId());
            pool.setApy(apy);
            pool.setLastUpdated(LocalDateTime.now());
            poolRepository.save(pool);
            log.info("Scanned pool: {} | APY: {}%", pool.getName(), apy);
        }

        log.info("Yield scan complete. Total pools scanned: {}", pools.size());
    }

    private BigDecimal fetchApyFromChain(String poolId) {
        if (web3jProperties.isSimulatedMode() || web3j == null) {
            // Simulated APY fluctuation: baseline +/- 1.2%
            BigDecimal currentApy = poolRepository.findById(poolId)
                    .map(Pool::getApy)
                    .orElse(new BigDecimal("5.00"));
            
            double fluctuation = (random.nextDouble() * 2.4) - 1.2; // Range [-1.2%, +1.2%]
            BigDecimal newApy = currentApy.add(BigDecimal.valueOf(fluctuation)).setScale(2, RoundingMode.HALF_UP);
            
            // Keep APYs in a realistic band [4.00%, 15.00%]
            if (newApy.compareTo(new BigDecimal("4.00")) < 0) {
                newApy = new BigDecimal("4.00");
            } else if (newApy.compareTo(new BigDecimal("15.00")) > 0) {
                newApy = new BigDecimal("15.00");
            }
            return newApy;
        }

        try {
            String contractAddress = poolContracts.get(poolId);
            if (contractAddress == null) {
                return new BigDecimal("5.00");
            }

            // Function calling getAPY() -> uint256 (where e.g., 850 represents 8.50% APY)
            Function apyFunc = new Function(
                    "getAPY",
                    Collections.emptyList(),
                    Collections.singletonList(new TypeReference<Uint256>() {})
            );

            String encodedCheck = FunctionEncoder.encode(apyFunc);
            EthCall ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(null, contractAddress, encodedCheck),
                    DefaultBlockParameterName.LATEST
            ).send();

            if (ethCall.hasError()) {
                log.warn("Error calling contract for pool {}: {}. Using fallback.", poolId, ethCall.getError().getMessage());
                return getFallbackApy(poolId);
            }

            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), apyFunc.getOutputParameters());
            if (results.isEmpty()) {
                return getFallbackApy(poolId);
            }

            java.math.BigInteger apyRaw = (java.math.BigInteger) results.get(0).getValue();
            // Convert e.g., 850 to 8.50
            return new BigDecimal(apyRaw).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.error("Failed to query pool APY from chain for {}: {}", poolId, e.getMessage());
            return getFallbackApy(poolId);
        }
    }

    private BigDecimal getFallbackApy(String poolId) {
        switch (poolId) {
            case "lorenzo-btc":
                return new BigDecimal("8.30").add(BigDecimal.valueOf(random.nextDouble() * 0.4));
            case "babylon-btc":
                return new BigDecimal("7.10").add(BigDecimal.valueOf(random.nextDouble() * 0.4));
            case "pell-btc":
                return new BigDecimal("8.90").add(BigDecimal.valueOf(random.nextDouble() * 0.4));
            default:
                return new BigDecimal("5.00");
        }
    }
}
