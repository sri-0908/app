package com.btcfi.repository;

import com.btcfi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = 'SUCCESS'")
    Long countSuccessfulByUserId(Long userId);
}
