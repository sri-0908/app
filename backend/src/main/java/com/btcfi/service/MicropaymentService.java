package com.btcfi.service;

import com.btcfi.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicropaymentService {

    private final Web3j web3j;
    private final Credentials agentCredentials;

    @Value("${agent.micropayment.fee-per-rebalance}")
    private Double feePerRebalance;

    @Value("${goat.network.contract.micropayment}")
    private String micropaymentContract;

    @Value("${goat.network.chain-id}")
    private Long chainId;

    private static final String CHARGE_FUNCTION_SELECTOR = "0x12345678";

    public BigDecimal chargeRebalanceFee(User user) {
        BigDecimal fee = BigDecimal.valueOf(feePerRebalance);
        try {
            BigInteger feeWei = Convert.toWei(fee.toPlainString(), Convert.Unit.ETHER).toBigInteger();
            BigInteger nonce = web3j.ethGetTransactionCount(agentCredentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = BigInteger.valueOf(100000L);

            String encodedData = CHARGE_FUNCTION_SELECTOR + String.format("%064s", user.getWalletAddress().substring(2)).replace(' ', '0') + String.format("%064x", feeWei);

            RawTransaction rawTx = RawTransaction.createTransaction(chainId, nonce, gasLimit, micropaymentContract, BigInteger.ZERO, encodedData, gasPrice, gasPrice.multiply(BigInteger.TWO));
            byte[] signedTx = TransactionEncoder.signMessage(rawTx, chainId, agentCredentials);
            EthSendTransaction response = web3j.ethSendRawTransaction(Numeric.toHexString(signedTx)).send();

            if (!response.hasError()) {
                log.info("Micropayment SUCCESS for user {}. Fee: {} BTC. TxHash: {}", user.getWalletAddress(), fee, response.getTransactionHash());
            }
        } catch (Exception e) {
            log.error("Micropayment error for user {}: {}", user.getWalletAddress(), e.getMessage());
        }
        return fee;
    }
}
