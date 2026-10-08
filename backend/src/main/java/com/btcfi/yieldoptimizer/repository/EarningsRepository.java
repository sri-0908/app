package com.btcfi.yieldoptimizer.repository;

import com.btcfi.yieldoptimizer.model.Earnings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EarningsRepository extends JpaRepository<Earnings, Integer> {
    List<Earnings> findByUserWalletAddressIgnoreCaseOrderByRecordedAtDesc(String walletAddress);
}
