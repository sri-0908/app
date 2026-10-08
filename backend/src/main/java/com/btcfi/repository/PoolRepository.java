package com.btcfi.repository;

import com.btcfi.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Long> {
    List<Pool> findByIsActiveTrueOrderByApyDesc();

    @Query("SELECT p FROM Pool p WHERE p.isActive = true ORDER BY p.apy DESC")
    List<Pool> findTopPoolsByApy();

    Optional<Pool> findByName(String name);

    @Query("SELECT p FROM Pool p WHERE p.isActive = true AND p.riskScore <= :maxRisk ORDER BY p.apy DESC")
    List<Pool> findPoolsByMaxRisk(int maxRisk);
}
