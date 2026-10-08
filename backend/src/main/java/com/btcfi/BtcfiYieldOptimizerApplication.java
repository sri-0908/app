package com.btcfi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BTCFi Yield Optimizer Agent - Main Application Entry Point
 * Monitors GOAT Network yield pools and auto-rebalances every 60 seconds.
 */
@SpringBootApplication
@EnableScheduling
public class BtcfiYieldOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BtcfiYieldOptimizerApplication.class, args);
        System.out.println("""
            +--------------------------------------------------+
            |   BTCFi Yield Optimizer Agent - GOAT Network     |
            |   Version 1.0.0 | AI-Powered Rebalancing         |
            +--------------------------------------------------+
            Agent started! Monitoring yield pools every 60 seconds.
            """);
    }
}
