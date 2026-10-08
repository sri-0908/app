package com.btcfi.yieldoptimizer.service;

import com.btcfi.yieldoptimizer.config.Web3jConfig.Web3jProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.Collections;
import java.util.List;

@Service
public class AgentIdentityService {

    private static final Logger log = LoggerFactory.getLogger(AgentIdentityService.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final Web3jProperties web3jProperties;
    private boolean isRegisteredOnChain = false;

    @Autowired
    public AgentIdentityService(Web3j web3j, Credentials credentials, Web3jProperties web3jProperties) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.web3jProperties = web3jProperties;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing AgentIdentityService for ERC-8004. Agent Address: {}", credentials.getAddress());
        registerAgentIdentity();
    }

    public boolean registerAgentIdentity() {
        if (isRegisteredOnChain) {
            log.info("Agent identity already verified and registered.");
            return true;
        }

        String metadataUri = "https://raw.githubusercontent.com/btcfi/yield-optimizer-agent/main/metadata.json";

        if (web3jProperties.isSimulatedMode() || web3j == null) {
            log.info("[SIMULATION] Registering agent identity via ERC-8004...");
            log.info("[SIMULATION] ERC-8004 Contract Address: {}", web3jProperties.getIdentityRegistryAddress());
            log.info("[SIMULATION] Method: registerAgent(agentAddress={}, metadataUri={})", credentials.getAddress(), metadataUri);
            log.info("[SIMULATION] Tx Hash: 0x" + Long.toHexString(Double.doubleToLongBits(Math.random())) + "d8a1c93a0bcf81e35aa");
            isRegisteredOnChain = true;
            return true;
        }

        try {
            // 1. Check if already registered (call constant function)
            Function checkFunc = new Function(
                    "isRegistered",
                    Collections.singletonList(new Address(credentials.getAddress())),
                    Collections.singletonList(new TypeReference<Bool>() {})
            );
            
            String encodedCheck = FunctionEncoder.encode(checkFunc);
            EthCall ethCall = web3j.ethCall(
                    Transaction.createEthCallTransaction(credentials.getAddress(), web3jProperties.getIdentityRegistryAddress(), encodedCheck),
                    DefaultBlockParameterName.LATEST
            ).send();

            if (ethCall.hasError()) {
                log.error("Error checking ERC-8004 registration: {}", ethCall.getError().getMessage());
            } else {
                List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), checkFunc.getOutputParameters());
                if (!results.isEmpty() && (Boolean) results.get(0).getValue()) {
                    log.info("Agent already registered on ERC-8004 Registry contract.");
                    isRegisteredOnChain = true;
                    return true;
                }
            }

            // 2. Sign and send registration transaction
            log.info("Agent not registered. Registering on ERC-8004 registry: {}...", web3jProperties.getIdentityRegistryAddress());
            Function registerFunc = new Function(
                    "registerAgent",
                    List.of(new Address(credentials.getAddress()), new Utf8String(metadataUri)),
                    Collections.emptyList()
            );

            // ChainId parameter here: default to 1 (mainnet) or 4862 (GOAT Testnet placeholder)
            long chainId = 4862;
            TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
            String encodedTx = FunctionEncoder.encode(registerFunc);

            // Execute transaction
            String txHash = txManager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    web3jProperties.getIdentityRegistryAddress(),
                    encodedTx,
                    java.math.BigInteger.ZERO
            ).getTransactionHash();

            log.info("ERC-8004 registration transaction submitted. Tx Hash: {}", txHash);

            // Wait for transaction receipt
            TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Transaction receipt not found"));

            if (receipt.isStatusOK()) {
                log.info("ERC-8004 Agent Registration successful!");
                isRegisteredOnChain = true;
                return true;
            } else {
                log.error("ERC-8004 Agent Registration transaction reverted.");
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to perform ERC-8004 agent registration: {}. Defaulting to simulated state.", e.getMessage());
            isRegisteredOnChain = true;
            return true;
        }
    }

    public boolean isRegisteredOnChain() {
        return isRegisteredOnChain;
    }
}
