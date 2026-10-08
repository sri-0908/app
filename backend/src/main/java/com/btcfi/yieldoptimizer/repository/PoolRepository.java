package com.btcfi.yieldoptimizer.repository;

import com.btcfi.yieldoptimizer.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolRepository extends JpaRepository<Pool, String> {
}
