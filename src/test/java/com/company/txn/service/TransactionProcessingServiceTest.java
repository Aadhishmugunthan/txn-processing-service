package com.company.txn.service;

import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.config.TxnTypeConfig;
import com.company.txn.drools.TransactionDecision;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionProcessingServiceTest {

    @Mock
    private TxnMappingConfig mappingConfig;

    @Mock
    private TransactionRepository repository;

    @Mock
    private KieContainer kieContainer;

    @Mock
    private KieSession kieSession;

    @InjectMocks
    private TransactionProcessingService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // ---------------- DROOLS ----------------
        when(kieContainer.newKieSession()).thenReturn(kieSession);

        doAnswer(invocation -> {
            TransactionDecision decision = invocation.getArgument(0);
            decision.setProcessTxn(true); // ALLOW
            return null;
        }).when(kieSession).insert(any(TransactionDecision.class));

        // ---------------- TXN CONFIG ----------------
        TxnFieldConfig amountField = new TxnFieldConfig();
        amountField.setSource("json");
        amountField.setPath("$.amount");
        amountField.setRequired(false);

        Map<String, TxnFieldConfig> txnFields = new HashMap<>();
        txnFields.put("AMOUNT", amountField);

        TxnTypeConfig txnTypeConfig = new TxnTypeConfig();
        txnTypeConfig.setTransaction(txnFields); // ✅ THIS FIXES THE NPE

        Map<String, TxnTypeConfig> mappings = new HashMap<>();
        mappings.put("PAYMENT", txnTypeConfig);

        when(mappingConfig.getMappings()).thenReturn(mappings);
    }

    @Test
    void shouldProcessTransaction_whenOperationIsAllow() {

        TransactionRequest request = new TransactionRequest();
        request.setTxnId(UUID.randomUUID().toString());
        request.setTxnType("PAYMENT");
        request.setOperation("A");
        request.setPayload("{\"amount\":1000}");

        when(repository.transactionExists(any())).thenReturn(false);

        String txnId = service.process(request);

        verify(repository).insertTransactionDynamic(any());
        verify(kieSession).fireAllRules();
        verify(kieSession).dispose();

        assert txnId != null;
    }
}

