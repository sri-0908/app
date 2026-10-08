package com.btcfi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Web3j Configuration - Connects to GOAT Network RPC endpoint.
 */
@Slf4j
@Configuration
public class Web3jConfig {

    @Value("${goat.network.rpc-url}")
    private String rpcUrl;

    @Value("${goat.network.agent-private-key}")
    private String agentPrivateKey;

    /**
     * Creates Web3j instance connected to GOAT Network RPC.
     */
    @Bean
    public Web3j web3j() {
        log.info("Connecting to GOAT Network RPC: {}", rpcUrl);
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("Connected to GOAT Network. Client: {}", clientVersion);
        } catch (Exception e) {
            log.warn("Could not verify GOAT Network connection: {}", e.getMessage());
        }
        return web3j;
    }

    /** Agent credentials derived from private key (stored securely in env var). */
    @Bean
    public Credentials agentCredentials() {
        log.info("Loading agent wallet credentials...");
        return Credentials.create(agentPrivateKey);
    }

    /** Default gas provider for GOAT Network transactions. */
    @Bean
    public ContractGasProvider gasProvider() {
        return new DefaultGasProvider();
    }
}
