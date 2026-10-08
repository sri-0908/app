package com.btcfi.repository;

import com.btcfi.model.Earning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EarningRepository extends JpaRepository<Earning, Long> {
    List<Earning> findByUserIdOrderByRecordedAtDesc(Long userId);

    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.userId = :userId")
    BigDecimal sumTotalEarningsByUserId(Long userId);
}
