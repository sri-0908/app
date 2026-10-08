package com.btcfi.yieldoptimizer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Configuration
public class Web3jConfig {

    private static final Logger log = LoggerFactory.getLogger(Web3jConfig.class);

    @Value("${goat.rpc-url}")
    private String rpcUrl;

    @Value("${goat.agent-private-key}")
    private String privateKey;

    @Value("${goat.identity-registry-address}")
    private String identityRegistryAddress;

    @Value("${goat.micropayment-address}")
    private String micropaymentAddress;

    @Value("${goat.simulated-mode:true}")
    private boolean simulatedMode;

    @Bean
    public Web3j web3j() {
        if (simulatedMode) {
            log.info("Simulated mode active. Web3j network client not connecting to live RPC.");
            return null;
        }
        try {
            log.info("Connecting to GOAT Network RPC at: {}", rpcUrl);
            return Web3j.build(new HttpService(rpcUrl));
        } catch (Exception e) {
            log.error("Failed to build Web3j client. Falling back to simulated mode.", e);
            this.simulatedMode = true;
            return null;
        }
    }

    @Bean
    public Credentials credentials() {
        if (simulatedMode) {
            log.info("Simulated mode active. Generating dummy EC keypair credentials.");
            try {
                return Credentials.create(Keys.createEcKeyPair());
            } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
                log.error("Failed to create dummy credentials", e);
                return Credentials.create("0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
            }
        }
        try {
            // Ensure hex prefix is present and standard
            String cleanKey = privateKey;
            if (cleanKey.startsWith("0x")) {
                cleanKey = cleanKey.substring(2);
            }
            return Credentials.create(cleanKey);
        } catch (Exception e) {
            log.error("Invalid agent private key provided: {}. Generating dummy EC keypair.", e.getMessage());
            try {
                return Credentials.create(Keys.createEcKeyPair());
            } catch (Exception ex) {
                return Credentials.create("0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
            }
        }
    }

    @Bean
    public Web3jProperties web3jProperties() {
        return new Web3jProperties(identityRegistryAddress, micropaymentAddress, simulatedMode);
    }

    public static class Web3jProperties {
        private final String identityRegistryAddress;
        private final String micropaymentAddress;
        private boolean simulatedMode;

        public Web3jProperties(String identityRegistryAddress, String micropaymentAddress, boolean simulatedMode) {
            this.identityRegistryAddress = identityRegistryAddress;
            this.micropaymentAddress = micropaymentAddress;
            this.simulatedMode = simulatedMode;
        }

        public String getIdentityRegistryAddress() {
            return identityRegistryAddress;
        }

        public String getMicropaymentAddress() {
            return micropaymentAddress;
        }

        public boolean isSimulatedMode() {
            return simulatedMode;
        }

        public void setSimulatedMode(boolean simulatedMode) {
            this.simulatedMode = simulatedMode;
        }
    }
}
