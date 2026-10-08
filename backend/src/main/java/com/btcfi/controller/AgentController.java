package com.btcfi.controller;

import com.btcfi.dto.RebalanceRequestDTO;
import com.btcfi.model.Transaction;
import com.btcfi.model.User;
import com.btcfi.service.RebalancerService;
import com.btcfi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final RebalancerService rebalancerService;
    private final UserService userService;

    @PostMapping("/rebalance")
    public ResponseEntity<?> triggerManualRebalance(@RequestBody RebalanceRequestDTO request) {
        User user = userService.registerOrGetUser(request.getWalletAddress());
        Transaction tx = rebalancerService.checkAndRebalanceUser(user);
        
        if (tx == null) {
            return ResponseEntity.ok(java.util.Map.of("status", "SKIPPED", "message", "No profitable rebalance found or on cooldown."));
        }
        return ResponseEntity.ok(tx);
    }
}
