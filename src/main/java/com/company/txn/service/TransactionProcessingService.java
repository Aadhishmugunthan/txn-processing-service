package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.config.TxnTypeConfig;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionProcessingService {

    private final TxnMappingConfig txnMappingConfig;
    private final TransactionRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionProcessingService(
            TxnMappingConfig txnMappingConfig,
            TransactionRepository repository
    ) {
        this.txnMappingConfig = txnMappingConfig;
        this.repository = repository;
    }

    @Transactional
    public String process(TransactionRequest request) {

        // Validate request
        if (request == null || request.getTxnType() == null || request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
        }

        // txnId is now required in the request
        if (request.getTxnId() == null || request.getTxnId().trim().isEmpty()) {
            throw new IllegalArgumentException("txnId is required");
        }

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(request.getPayload(), Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payload format: " + e.getMessage());
        }

        TxnTypeConfig txnTypeConfig =
                txnMappingConfig.getMappings().get(request.getTxnType());

        if (txnTypeConfig == null) {
            throw new IllegalArgumentException("Unsupported txnType: " + request.getTxnType());
        }

        // Get txnId from request (no UUID generation)
        String txnId = request.getTxnId();

        // Check if transaction exists in database
        boolean exists = repository.transactionExists(txnId);

        Map<String, Object> txnColumns = new HashMap<>();
        txnColumns.put("TXN_ID", txnId);

        for (Map.Entry<String, TxnFieldConfig> entry :
                txnTypeConfig.getTransaction().entrySet()) {

            if (entry.getKey().equals("TXN_ID")) continue;

            TxnFieldConfig cfg = entry.getValue();
            Object value = resolve(cfg, payload);

            if (cfg.isRequired() && value == null) {
                throw new IllegalArgumentException("Missing required field: " + entry.getKey());
            }

            txnColumns.put(entry.getKey(), value);
        }

        // INSERT or UPDATE based on existence
        if (exists) {
            repository.updateTransactionDynamic(txnId, txnColumns);
        } else {
            repository.insertTransactionDynamic(txnColumns);
        }

        // INSERT or UPDATE TRANSACTION_DETAILS
        if (txnTypeConfig.getTransactionDetails() != null) {

            Map<String, Object> detailColumns = new HashMap<>();

            for (TxnDetailConfig d : txnTypeConfig.getTransactionDetails()) {
                Object value = safeResolve(d, payload);

                if (d.isRequired() && value == null) {
                    throw new IllegalArgumentException("Missing required transaction_detail: " + d.getColumn());
                }

                detailColumns.put(d.getColumn(), value);
            }

            if (exists) {
                repository.updateTransactionDetailsOnce(txnId, detailColumns);
            } else {
                repository.insertTransactionDetailsOnce(txnId, detailColumns);
            }
        }

        // DELETE and RE-INSERT Addresses (simpler approach)
        if (txnTypeConfig.getAddresses() != null) {

            if (exists) {
                repository.deleteTransactionAddresses(txnId);
            }

            var addrCfg = txnTypeConfig.getAddresses();

            for (var addrDef : addrCfg.getDefinitions()) {

                // Generate addressId only (not txnId)
                String addressId = UUID.randomUUID().toString();
                Map<String, Object> addrJson =
                        JsonPath.read(payload, addrDef.getJsonPath());

                repository.insertTransactionAddress(
                        addressId,
                        txnId,
                        addrDef.getAddressType(),
                        String.valueOf(addrJson.get("line1")),
                        String.valueOf(addrJson.get("city")),
                        "INDIA"
                );
            }
        }

        // DELETE and RE-INSERT Transaction Status (simpler approach)
        if (txnTypeConfig.getStatus() != null &&
                txnTypeConfig.getStatus().getInitial() != null) {

            if (exists) {
                repository.deleteTransactionStatus(txnId);
            }

            var statusCfg = txnTypeConfig.getStatus().getInitial();

            // Generate statusId only (not txnId)
            repository.insertTransactionStatus(
                    UUID.randomUUID().toString(),
                    txnId,
                    statusCfg.getCurrent_status(),
                    statusCfg.getRemarks()
            );
        }

        return txnId;  // Return the txnId from request
    }

    private Object resolve(TxnFieldConfig cfg, Map<String, Object> payload) {
        return switch (cfg.getSource()) {
            case "generated" -> UUID.randomUUID().toString();
            case "constant" -> cfg.getValue();
            case "json" -> JsonPath.read(payload, cfg.getPath());
            default -> throw new IllegalStateException("Invalid source: " + cfg.getSource());
        };
    }

    private Object safeResolve(TxnDetailConfig cfg, Map<String, Object> payload) {
        try {
            return resolve(cfg, payload);
        } catch (Exception e) {
            return null;
        }
    }
}