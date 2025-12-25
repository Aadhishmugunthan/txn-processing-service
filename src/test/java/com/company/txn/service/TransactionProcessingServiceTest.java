package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.config.TxnTypeConfig;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionProcessingServiceTest {

    private TransactionRepository repository;
    private TxnMappingConfig mappingConfig;
    private TransactionProcessingService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(TransactionRepository.class);
        mappingConfig = new TxnMappingConfig();
        service = new TransactionProcessingService(mappingConfig, repository);
    }

    @Test
    void testPaymentTransactionProcessing() {

        // 🔥 Prepare transaction config
        TxnTypeConfig txnTypeConfig = new TxnTypeConfig();

        Map<String, TxnFieldConfig> transactionFields = new HashMap<>();
        TxnFieldConfig amountField = new TxnFieldConfig();
        amountField.setSource("json");
        amountField.setPath("$.amount");
        amountField.setRequired(true);
        transactionFields.put("amount", amountField);

        txnTypeConfig.setTransaction(transactionFields);

        // 🔥 Status Config
        TxnTypeConfig.StatusConfig statusConfig = new TxnTypeConfig.StatusConfig();
        TxnTypeConfig.StatusConfig.InitialStatus initial = new TxnTypeConfig.StatusConfig.InitialStatus();
        initial.setCurrent_status("ACCEPTED");
        initial.setRemarks("Mock");
        statusConfig.setInitial(initial);
        txnTypeConfig.setStatus(statusConfig);

        Map<String, TxnTypeConfig> mappings = new HashMap<>();
        mappings.put("PAYMENT", txnTypeConfig);
        mappingConfig.setMappings(mappings);

        // 🔥 Payload as MAP (NOT String anymore)
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", 1000);

        TransactionRequest request = new TransactionRequest();
        request.setTxnType("PAYMENT");
        request.setPayload(payload);

        when(repository.insertTransactionDynamic(any())).thenReturn(UUID.randomUUID().toString());

        String txnId = service.process(request);
        assertNotNull(txnId);

        verify(repository, times(1)).insertTransactionDynamic(any());
        verify(repository, times(1))
                .insertTransactionStatus(any(), any(), eq("ACCEPTED"), eq("Mock"));
    }
}
