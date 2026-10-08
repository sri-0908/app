package com.btcfi.scheduler;

import com.btcfi.service.RebalancerService;
import com.btcfi.service.YieldScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentScheduler {

    private final YieldScannerService yieldScannerService;
    private final RebalancerService rebalancerService;

    /** Runs every 60 seconds */
    @Scheduled(fixedDelayString = "${agent.rebalance.scan-interval-ms:60000}")
    public void runAgentTasks() {
        log.info("--- Agent Scheduler Cycle Started ---");
        
        // 1. Scan network for latest yields
        yieldScannerService.scanAndUpdatePools();
        
        // 2. Evaluate rules and rebalance users if needed
        rebalancerService.runRebalanceCheck();
        
        log.info("--- Agent Scheduler Cycle Completed ---");
    }
}
