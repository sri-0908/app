package com.btcfi.service;

import com.btcfi.model.Pool;
import com.btcfi.model.Transaction;
import com.btcfi.model.User;
import com.btcfi.repository.PoolRepository;
import com.btcfi.repository.TransactionRepository;
import com.btcfi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RebalancerService - AI Rule-Based Decision Engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RebalancerService {

    private final Web3j web3j;
    private final Credentials agentCredentials;
    private final UserRepository userRepository;
    private final PoolRepository poolRepository;
    private final TransactionRepository transactionRepository;
    private final MicropaymentService micropaymentService;
    private final YieldScannerService yieldScannerService;

    @Value("${agent.rebalance.apy-threshold}")
    private Double apyThreshold;

    @Value("${goat.network.chain-id}")
    private Long chainId;

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(300000L);

    @Transactional
    public void runRebalanceCheck() {
        log.info("Running rebalance check for all active users...");
        List<User> activeUsers = userRepository.findAllActiveUsers();
        for (User user : activeUsers) {
            try {
                checkAndRebalanceUser(user);
            } catch (Exception e) {
                log.error("Error rebalancing user {}: {}", user.getWalletAddress(), e.getMessage());
            }
        }
    }

    @Transactional
    public Transaction checkAndRebalanceUser(User user) {
        Optional<Pool> currentPoolOpt = user.getCurrentPoolId() != null 
            ? poolRepository.findById(user.getCurrentPoolId()) : Optional.empty();

        Pool bestPool = yieldScannerService.getBestPool(7);
        if (bestPool == null) return null;

        if (currentPoolOpt.isEmpty()) {
            return executeRebalance(user, null, bestPool, 0.0, bestPool.getApy());
        }

        Pool currentPool = currentPoolOpt.get();
        double apyDelta = bestPool.getApy() - currentPool.getApy();

        if (apyDelta <= apyThreshold || currentPool.getId().equals(bestPool.getId())) {
            return null;
        }

        if (user.getLastRebalanceAt() != null && user.getLastRebalanceAt().isAfter(LocalDateTime.now().minusMinutes(10))) {
            return null; // Cooldown
        }

        return executeRebalance(user, currentPool, bestPool, currentPool.getApy(), bestPool.getApy());
    }

    @Transactional
    public Transaction executeRebalance(User user, Pool fromPool, Pool toPool, double apyBefore, double apyAfter) {
        Transaction txRecord = Transaction.builder()
            .userId(user.getId())
            .fromPool(fromPool != null ? fromPool.getName() : null)
            .toPool(toPool.getName())
            .amount(BigDecimal.valueOf(user.getTotalDeposited() != null ? user.getTotalDeposited() : 0.01))
            .apyBefore(apyBefore)
            .apyAfter(apyAfter)
            .status("PENDING")
            .build();

        try {
            BigInteger nonce = web3j.ethGetTransactionCount(agentCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            
            String txData = encodeRebalanceCall(user.getWalletAddress(), fromPool != null ? fromPool.getId() : 0L, toPool.getId());

            RawTransaction rawTx = RawTransaction.createTransaction(
                chainId, nonce, GAS_LIMIT, toPool.getContractAddress(), BigInteger.ZERO, txData, gasPrice, gasPrice.multiply(BigInteger.TWO)
            );

            byte[] signedTx = TransactionEncoder.signMessage(rawTx, chainId, agentCredentials);
            EthSendTransaction sendResponse = web3j.ethSendRawTransaction(Numeric.toHexString(signedTx)).send();

            if (sendResponse.hasError()) {
                txRecord.setStatus("FAILED");
                txRecord.setErrorMessage(sendResponse.getError().getMessage());
            } else {
                txRecord.setTxHash(sendResponse.getTransactionHash());
                txRecord.setStatus("SUCCESS");
                txRecord.setGasUsed(GAS_LIMIT.longValue());
                txRecord.setFee(micropaymentService.chargeRebalanceFee(user));

                user.setCurrentPool(toPool.getName());
                user.setCurrentPoolId(toPool.getId());
                user.setLastRebalanceAt(LocalDateTime.now());
                userRepository.save(user);
            }
        } catch (Exception e) {
            txRecord.setStatus("FAILED");
            txRecord.setErrorMessage(e.getMessage());
        }

        return transactionRepository.save(txRecord);
    }

    private String encodeRebalanceCall(String userAddress, Long fromPoolId, Long toPoolId) {
        String selector = "0xa9059cbb";
        String paddedAddress = String.format("%064s", userAddress.substring(2)).replace(' ', '0');
        String paddedFrom = String.format("%064x", fromPoolId);
        String paddedTo = String.format("%064x", toPoolId);
        return selector + paddedAddress + paddedFrom + paddedTo;
    }
}
