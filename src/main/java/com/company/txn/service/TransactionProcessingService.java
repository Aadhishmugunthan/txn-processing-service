package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.config.TxnTypeConfig;
import com.company.txn.drools.TransactionDecision;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionProcessingService {

    private final TxnMappingConfig txnMappingConfig;
    private final TransactionRepository repository;
    private final KieContainer kieContainer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionProcessingService(
            TxnMappingConfig txnMappingConfig,
            TransactionRepository repository,
            KieContainer kieContainer
    ) {
        this.txnMappingConfig = txnMappingConfig;
        this.repository = repository;
        this.kieContainer = kieContainer;
    }

    @Transactional
    public String process(TransactionRequest request) {

        // ================= VALIDATION =================
        if (request == null || request.getTxnType() == null || request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
        }

        if (request.getTxnId() == null || request.getTxnId().trim().isEmpty()) {
            throw new IllegalArgumentException("txnId is required");
        }

        // ================= DROOLS DECISION =================
        TransactionDecision decision =
                new TransactionDecision(request.getOperation());

        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(decision);
        kieSession.fireAllRules();
        kieSession.dispose();

        if (!decision.isProcessTxn()) {
            // Rule decided to ignore transaction
            return request.getTxnId(); // or return "IGNORED" if you prefer
        }

        // ================= PAYLOAD PARSE =================
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

        String txnId = request.getTxnId();

        boolean exists = repository.transactionExists(txnId);

        // ================= TRANSACTION TABLE =================
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

        if (exists) {
            repository.updateTransactionDynamic(txnId, txnColumns);
        } else {
            repository.insertTransactionDynamic(txnColumns);
        }

        // ================= TRANSACTION_DETAILS =================
        if (txnTypeConfig.getTransactionDetails() != null) {

            Map<String, Object> detailColumns = new HashMap<>();

            for (TxnDetailConfig d : txnTypeConfig.getTransactionDetails()) {
                Object value = safeResolve(d, payload);

                if (d.isRequired() && value == null) {
                    throw new IllegalArgumentException(
                            "Missing required transaction_detail: " + d.getColumn());
                }

                detailColumns.put(d.getColumn(), value);
            }

            if (exists) {
                repository.updateTransactionDetailsOnce(txnId, detailColumns);
            } else {
                repository.insertTransactionDetailsOnce(txnId, detailColumns);
            }
        }

        // ================= TRANSACTION_ADDRESS =================
        if (txnTypeConfig.getAddresses() != null) {

            if (exists) {
                repository.deleteTransactionAddresses(txnId);
            }

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

        // ================= TRANSACTION_STATUS =================
        if (txnTypeConfig.getStatus() != null &&
                txnTypeConfig.getStatus().getInitial() != null) {

            if (exists) {
                repository.deleteTransactionStatus(txnId);
            }

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

    // ================= HELPER METHODS =================

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
