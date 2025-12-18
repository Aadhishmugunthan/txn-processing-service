package com.company.txn.service;

import com.company.txn.config.TxnMappingConfig;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProcessingService {

    private final TxnMappingConfig txnMappingConfig;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionProcessingService(
            TxnMappingConfig txnMappingConfig,
            TransactionRepository transactionRepository
    ) {
        this.txnMappingConfig = txnMappingConfig;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public String process(TransactionRequest request) {

        if (request == null || request.getTxnType() == null || request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
        }

        // config lookup
        TxnMappingConfig.TxnTypeConfig txnConfig =
                txnMappingConfig.get(request.getTxnType());

        if (txnConfig == null) {
            throw new IllegalArgumentException("Unsupported txnType: " + request.getTxnType());
        }

        try {
            // parse payload
            JsonNode payload = objectMapper.readTree(request.getPayload());

            double amount = payload.get("amount").asDouble();
            String currency = payload.get("currency").asText();

            // INSERT INTO DB
            return transactionRepository.insertTransaction(
                    request.getTxnType(),
                    amount,
                    currency
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid payload JSON", e);
        }
    }
}
