package com.btcfi.service;

import com.btcfi.dto.TransactionDTO;
import com.btcfi.dto.UserPositionDTO;
import com.btcfi.model.Pool;
import com.btcfi.model.Transaction;
import com.btcfi.model.User;
import com.btcfi.repository.EarningRepository;
import com.btcfi.repository.PoolRepository;
import com.btcfi.repository.TransactionRepository;
import com.btcfi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PoolRepository poolRepository;
    private final TransactionRepository transactionRepository;
    private final EarningRepository earningRepository;

    @Transactional
    public User registerOrGetUser(String walletAddress) {
        return userRepository.findByWalletAddress(walletAddress.toLowerCase())
            .orElseGet(() -> userRepository.save(User.builder().walletAddress(walletAddress.toLowerCase()).totalDeposited(0.0).isActive(true).build()));
    }

    @Transactional(readOnly = true)
    public UserPositionDTO getUserPosition(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Double currentApy = user.getCurrentPoolId() != null ? poolRepository.findById(user.getCurrentPoolId()).map(Pool::getApy).orElse(0.0) : 0.0;
        BigDecimal earnings = earningRepository.sumTotalEarningsByUserId(userId);
        if (earnings == null) earnings = BigDecimal.ZERO;
        
        List<TransactionDTO> txDTOs = transactionRepository.findByUserIdOrderByTimestampDesc(userId).stream().limit(10)
            .map(tx -> TransactionDTO.builder().id(tx.getId()).fromPool(tx.getFromPool()).toPool(tx.getToPool()).amount(tx.getAmount()).fee(tx.getFee()).txHash(tx.getTxHash()).status(tx.getStatus()).apyBefore(tx.getApyBefore()).apyAfter(tx.getApyAfter()).timestamp(tx.getTimestamp()).build())
            .collect(Collectors.toList());

        return UserPositionDTO.builder().userId(user.getId()).walletAddress(user.getWalletAddress()).currentPool(user.getCurrentPool()).currentApy(currentApy).totalEarnings(earnings).totalDeposited(user.getTotalDeposited()).lastRebalanceAt(user.getLastRebalanceAt()).joinedAt(user.getJoinedAt()).recentTransactions(txDTOs).totalRebalances(transactionRepository.countSuccessfulByUserId(userId).intValue()).build();
    }
}
