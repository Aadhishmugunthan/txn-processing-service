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

        if (request == null || request.getTxnType() == null || request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
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

        // Always generate transaction ID here
        String txnId = UUID.randomUUID().toString();

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

        repository.insertTransactionDynamic(txnColumns);

        // Insert TRANSACTION_DETAILS
        if (txnTypeConfig.getTransactionDetails() != null) {

            Map<String, Object> detailColumns = new HashMap<>();

            for (TxnDetailConfig d : txnTypeConfig.getTransactionDetails()) {
                Object value = safeResolve(d, payload);

                if (d.isRequired() && value == null) {
                    throw new IllegalArgumentException("Missing required transaction_detail: " + d.getColumn());
                }

                detailColumns.put(d.getColumn(), value);
            }

            repository.insertTransactionDetailsOnce(txnId, detailColumns);
        }

        // Insert Addresses
        if (txnTypeConfig.getAddresses() != null) {

            var addrCfg = txnTypeConfig.getAddresses();

            for (var addrDef : addrCfg.getDefinitions()) {

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

        // Insert Transaction Status
        if (txnTypeConfig.getStatus() != null &&
                txnTypeConfig.getStatus().getInitial() != null) {

            var statusCfg = txnTypeConfig.getStatus().getInitial();

            repository.insertTransactionStatus(
                    UUID.randomUUID().toString(),
                    txnId,
                    statusCfg.getCurrent_status(),
                    statusCfg.getRemarks()
            );
        }

        return txnId;
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