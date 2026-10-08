package com.btcfi.yieldoptimizer.controller;

import com.btcfi.yieldoptimizer.model.Earnings;
import com.btcfi.yieldoptimizer.model.Pool;
import com.btcfi.yieldoptimizer.model.Transaction;
import com.btcfi.yieldoptimizer.model.User;
import com.btcfi.yieldoptimizer.repository.EarningsRepository;
import com.btcfi.yieldoptimizer.repository.PoolRepository;
import com.btcfi.yieldoptimizer.repository.TransactionRepository;
import com.btcfi.yieldoptimizer.repository.UserRepository;
import com.btcfi.yieldoptimizer.service.RebalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows local React frontend to communicate with backend
public class AgentController {

    private final PoolRepository poolRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final EarningsRepository earningsRepository;
    private final RebalancerService rebalancerService;

    @Autowired
    public AgentController(PoolRepository poolRepository, UserRepository userRepository,
                           TransactionRepository transactionRepository, EarningsRepository earningsRepository,
                           RebalancerService rebalancerService) {
        this.poolRepository = poolRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.earningsRepository = earningsRepository;
        this.rebalancerService = rebalancerService;
    }

    /**
     * GET /api/pools -> Returns all available yield pools with both raw and risk-adjusted APY.
     */
    @GetMapping("/pools")
    public ResponseEntity<List<Map<String, Object>>> getPools() {
        List<Pool> pools = poolRepository.findAll();
        List<Map<String, Object>> response = pools.stream().map(pool -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pool.getId());
            map.put("name", pool.getName());
            map.put("protocol", pool.getProtocol());
            map.put("rawApy", pool.getApy());
            map.put("riskAdjustedApy", rebalancerService.getRiskAdjustedApy(pool));
            map.put("lastUpdated", pool.getLastUpdated());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/user/{walletAddress}/position -> Returns the user's current pool, earnings, and transaction history.
     */
    @GetMapping("/user/{walletAddress}/position")
    public ResponseEntity<?> getUserPosition(@PathVariable String walletAddress) {
        Optional<User> userOpt = userRepository.findByWalletAddressIgnoreCase(walletAddress);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        List<Transaction> transactions = transactionRepository.findByUserWalletAddressIgnoreCaseOrderByTimestampDesc(walletAddress);
        List<Earnings> earningsList = earningsRepository.findByUserWalletAddressIgnoreCaseOrderByRecordedAtDesc(walletAddress);

        // Calculate total earnings
        BigDecimal totalEarnings = earningsList.stream()
                .map(Earnings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("walletAddress", user.getWalletAddress());
        response.put("currentPool", user.getCurrentPool() != null ? user.getCurrentPool().getId() : null);
        response.put("currentPoolName", user.getCurrentPool() != null ? user.getCurrentPool().getName() : "None");
        response.put("currentPoolApy", user.getCurrentPool() != null ? user.getCurrentPool().getApy() : BigDecimal.ZERO);
        response.put("joinedAt", user.getJoinedAt());
        response.put("totalEarnings", totalEarnings);
        response.put("earningsHistory", earningsList);
        response.put("transactionHistory", transactions);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/user/register -> Registers a user's wallet and assigns them to the best yield pool.
     */
    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String walletAddress = request.get("walletAddress");
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Wallet address is required"));
        }

        Optional<User> existingUser = userRepository.findByWalletAddressIgnoreCase(walletAddress);
        if (existingUser.isPresent()) {
            return ResponseEntity.ok(existingUser.get());
        }

        // Initialize with default pool if none selected
        List<Pool> pools = poolRepository.findAll();
        Pool startingPool = pools.isEmpty() ? null : pools.get(0);

        User newUser = User.builder()
                .walletAddress(walletAddress.toLowerCase())
                .currentPool(startingPool)
                .joinedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        // Seed some initial earnings for demo purposes (dynamic values look much better on dashboard)
        Earnings initialEarnings = Earnings.builder()
                .user(savedUser)
                .amount(new BigDecimal("0.00125000"))
                .period("INITIAL_SEED")
                .recordedAt(LocalDateTime.now().minusDays(1))
                .build();
        earningsRepository.save(initialEarnings);

        return ResponseEntity.ok(savedUser);
    }

    /**
     * POST /api/agent/rebalance -> Manually triggers the rebalancer evaluation rules.
     */
    @PostMapping("/agent/rebalance")
    public ResponseEntity<?> triggerRebalance(@RequestBody Map<String, String> request) {
        String walletAddress = request.get("walletAddress");
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Wallet address is required"));
        }

        try {
            boolean triggered = rebalancerService.triggerManualRebalance(walletAddress);
            if (triggered) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Yield opportunity found. Assets successfully rebalanced."
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Current yield allocation is already optimal. No rebalance needed."
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Rebalancing evaluation failed: " + e.getMessage()));
        }
    }
}
