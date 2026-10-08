package com.btcfi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentIdentityService {

    private final Web3j web3j;
    private final Credentials agentCredentials;

    @Value("${goat.network.contract.erc8004-registry}")
    private String registryContract;

    @Value("${agent.identity.agent-name}")
    private String agentName;

    @Value("${agent.identity.agent-version}")
    private String agentVersion;

    @Value("${goat.network.chain-id}")
    private Long chainId;

    private String registeredAgentId;

    @EventListener(ApplicationReadyEvent.class)
    public void registerAgentOnStartup() {
        log.info("Registering ERC-8004 Agent Identity...");
        try {
            BigInteger nonce = web3j.ethGetTransactionCount(agentCredentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            
            String encodedData = "0xd1f57894" + String.format("%064s", agentCredentials.getAddress().substring(2)).replace(' ', '0') 
                + String.format("%-64s", Numeric.toHexString(agentName.getBytes(StandardCharsets.UTF_8)).substring(2)).replace(' ', '0');

            RawTransaction rawTx = RawTransaction.createTransaction(chainId, nonce, BigInteger.valueOf(200000L), registryContract, BigInteger.ZERO, encodedData, gasPrice, gasPrice.multiply(BigInteger.TWO));
            byte[] signedTx = TransactionEncoder.signMessage(rawTx, chainId, agentCredentials);
            EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(signedTx)).send();

            if (!response.hasError()) {
                registeredAgentId = agentCredentials.getAddress();
                log.info("Agent identity registered! TX: {}", response.getTransactionHash());
            }
        } catch (Exception e) {
            log.warn("Agent identity registration failed (non-critical): {}", e.getMessage());
        }
    }
}
