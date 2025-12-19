package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
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

    public TransactionProcessingService(
            TxnMappingConfig txnMappingConfig,
            TransactionRepository repository
    ) {
        this.txnMappingConfig = txnMappingConfig;
        this.repository = repository;
    }

    @Transactional
    public String process(TransactionRequest request) {

        if (request == null ||
                request.getTxnType() == null ||
                request.getPayload() == null) {
            throw new IllegalArgumentException("txnType and payload are required");
        }

        // 1️⃣ Load txn config
        TxnMappingConfig.TxnTypeConfig txnTypeConfig =
                txnMappingConfig.getMappings().get(request.getTxnType());

        if (txnTypeConfig == null) {
            throw new IllegalArgumentException(
                    "Unsupported txnType: " + request.getTxnType()
            );
        }

        // 2️⃣ Build transaction table
        Map<String, Object> txnColumns = new HashMap<>();

        for (Map.Entry<String, TxnFieldConfig> entry
                : txnTypeConfig.getTransaction().entrySet()) {

            TxnFieldConfig cfg = entry.getValue();
            Object value = resolve(cfg, request.getPayload());

            if (cfg.isRequired() && value == null) {
                throw new IllegalArgumentException(
                        "Missing required field: " + entry.getKey()
                );
            }

            txnColumns.put(entry.getKey(), value);
        }

        // 3️⃣ Insert transaction
        String txnId = repository.insertTransactionDynamic(txnColumns);

        // 4️⃣ Insert transaction_details
        if (txnTypeConfig.getTransactionDetails() != null) {
            for (TxnDetailConfig d : txnTypeConfig.getTransactionDetails()) {

                Object value = safeResolve(d, request.getPayload());

                if (d.isRequired() && value == null) {
                    throw new IllegalArgumentException(
                            "Missing required transaction_detail: " + d.getColumn()
                    );
                }

                repository.insertTransactionDetail(
                        txnId,
                        d.getColumn(),
                        value
                );
            }
        }

        // 5️⃣ Insert transaction_address (ONLY if config exists)
        if (txnTypeConfig.getAddresses() != null) {

            var addrCfg = txnTypeConfig.getAddresses();

            // Validate expectedSize
            if (addrCfg.getExpectedSize() != addrCfg.getDefinitions().size()) {
                throw new IllegalArgumentException(
                        "Expected " + addrCfg.getExpectedSize() +
                                " address(es) but found " + addrCfg.getDefinitions().size()
                );
            }

            for (var addrDef : addrCfg.getDefinitions()) {

                String addressId = UUID.randomUUID().toString();

                Map<String, Object> addrJson =
                        JsonPath.read(request.getPayload(), addrDef.getJsonPath());

                String line1 = String.valueOf(addrJson.get("line1"));
                String city  = String.valueOf(addrJson.get("city"));
                String country = "INDIA"; // from config (constant)

                repository.insertTransactionAddress(
                        addressId,
                        txnId,
                        addrDef.getAddressType(), // NEVER from payload
                        line1,
                        city,
                        country
                );
            }
        }

        return txnId;
    }

    // ---------- helpers ----------

    private Object resolve(TxnFieldConfig cfg, String payload) {
        return switch (cfg.getSource()) {
            case "generated" -> UUID.randomUUID().toString();
            case "constant" -> cfg.getValue();
            case "json" -> JsonPath.read(payload, cfg.getPath());
            default -> throw new IllegalStateException(
                    "Invalid source: " + cfg.getSource()
            );
        };
    }

    private Object safeResolve(TxnDetailConfig cfg, String payload) {
        try {
            return resolve(cfg, payload);
        } catch (Exception e) {
            return null;
        }
    }
}
