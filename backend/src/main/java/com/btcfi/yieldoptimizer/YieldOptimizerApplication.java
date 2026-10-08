package com.btcfi.yieldoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YieldOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YieldOptimizerApplication.class, args);
    }
}
