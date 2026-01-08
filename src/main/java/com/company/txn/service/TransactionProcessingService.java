package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.config.TxnTypeConfig;
import com.company.txn.drools.TransactionDecision;
import com.company.txn.exception.DroolsExecutionException;
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

        // ---------- BASIC VALIDATION ----------
        if (request == null || request.getTxnType() == null || request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
        }

        if (request.getTxnId() == null || request.getTxnId().trim().isEmpty()) {
            throw new IllegalArgumentException("txnId is required");
        }

        if (request.getOperation() == null) {
            throw new IllegalArgumentException("operation is required");
        }

        // ---------- PARSE PAYLOAD ----------
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(request.getPayload(), Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payload format");
        }

        // ---------- DROOLS DECISION ----------
        TransactionDecision decision = new TransactionDecision();
        decision.setOperation(request.getOperation());

        try {
            KieSession kieSession = kieContainer.newKieSession();
            kieSession.insert(decision);
            kieSession.fireAllRules();
            kieSession.dispose();
        } catch (Exception e) {
            throw new DroolsExecutionException("Error while executing Drools rules", e);
        }

        // ---------- IGNORE TRANSACTION ----------
        if (!decision.isProcessTxn()) {
            return request.getTxnId(); // ignore → no DB operations
        }

        // ---------- CONFIG LOOKUP ----------
        TxnTypeConfig txnTypeConfig =
                txnMappingConfig.getMappings().get(request.getTxnType());

        if (txnTypeConfig == null) {
            throw new IllegalArgumentException("Unsupported txnType: " + request.getTxnType());
        }

        String txnId = request.getTxnId();
        boolean exists = repository.transactionExists(txnId);

        // ---------- TRANSACTION TABLE ----------
        Map<String, Object> txnColumns = new HashMap<>();
        txnColumns.put("TXN_ID", txnId);

        for (Map.Entry<String, TxnFieldConfig> entry :
                txnTypeConfig.getTransaction().entrySet()) {

            if ("TXN_ID".equals(entry.getKey())) continue;

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

        // ---------- TRANSACTION DETAILS ----------
        if (txnTypeConfig.getTransactionDetails() != null) {
            Map<String, Object> detailColumns = new HashMap<>();

            for (TxnDetailConfig d : txnTypeConfig.getTransactionDetails()) {
                Object value = safeResolve(d, payload);

                if (d.isRequired() && value == null) {
                    throw new IllegalArgumentException(
                            "Missing required transaction detail: " + d.getColumn());
                }

                detailColumns.put(d.getColumn(), value);
            }

            if (exists) {
                repository.updateTransactionDetailsOnce(txnId, detailColumns);
            } else {
                repository.insertTransactionDetailsOnce(txnId, detailColumns);
            }
        }

        // ---------- ADDRESSES ----------
        if (txnTypeConfig.getAddresses() != null) {

            if (exists) {
                repository.deleteTransactionAddresses(txnId);
            }

            for (var addrDef : txnTypeConfig.getAddresses().getDefinitions()) {
                Map<String, Object> addrJson =
                        JsonPath.read(payload, addrDef.getJsonPath());

                repository.insertTransactionAddress(
                        UUID.randomUUID().toString(),
                        txnId,
                        addrDef.getAddressType(),
                        String.valueOf(addrJson.get("line1")),
                        String.valueOf(addrJson.get("city")),
                        "INDIA"
                );
            }
        }

        // ---------- STATUS ----------
        if (txnTypeConfig.getStatus() != null &&
                txnTypeConfig.getStatus().getInitial() != null) {

            if (exists) {
                repository.deleteTransactionStatus(txnId);
            }

            var status = txnTypeConfig.getStatus().getInitial();

            repository.insertTransactionStatus(
                    UUID.randomUUID().toString(),
                    txnId,
                    status.getCurrent_status(),
                    status.getRemarks()
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
