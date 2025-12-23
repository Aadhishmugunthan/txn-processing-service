package com.company.txn.service;

import com.company.txn.config.TxnDetailConfig;
import com.company.txn.config.TxnFieldConfig;
import com.company.txn.config.TxnMappingConfig;
import com.company.txn.model.TransactionRequest;
import com.company.txn.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionProcessingServiceTest {

    @Mock
    private TxnMappingConfig txnMappingConfig;

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionProcessingService service;

    @BeforeEach
    void setUp() {
        TxnMappingConfig.TxnTypeConfig paymentConfig = new TxnMappingConfig.TxnTypeConfig();

        Map<String, TxnFieldConfig> paymentFields = new HashMap<>();

        TxnFieldConfig idField = new TxnFieldConfig();
        idField.setSource("generated");
        idField.setRequired(true);
        paymentFields.put("txn_id", idField);

        TxnFieldConfig typeField = new TxnFieldConfig();
        typeField.setSource("constant");
        typeField.setValue("PAYMENT");
        paymentFields.put("txn_type", typeField);

        TxnFieldConfig amountField = new TxnFieldConfig();
        amountField.setSource("json");
        amountField.setPath("$.amount");
        paymentFields.put("amount", amountField);

        TxnFieldConfig currencyField = new TxnFieldConfig();
        currencyField.setSource("json");
        currencyField.setPath("$.currency");
        paymentFields.put("currency", currencyField);

        paymentConfig.setTransaction(paymentFields);

        List<TxnDetailConfig> detailConfigs = new ArrayList<>();
        TxnDetailConfig merchantConfig = new TxnDetailConfig();
        merchantConfig.setColumn("merchant_id");
        merchantConfig.setSource("json");
        merchantConfig.setPath("$.merchantId");
        detailConfigs.add(merchantConfig);
        paymentConfig.setTransactionDetails(detailConfigs);

        TxnMappingConfig.TxnTypeConfig refundConfig = new TxnMappingConfig.TxnTypeConfig();
        Map<String, TxnFieldConfig> refundFields = new HashMap<>();

        TxnFieldConfig refundIdField = new TxnFieldConfig();
        refundIdField.setSource("generated");
        refundFields.put("txn_id", refundIdField);

        TxnFieldConfig refundTypeField = new TxnFieldConfig();
        refundTypeField.setSource("constant");
        refundTypeField.setValue("REFUND");
        refundFields.put("txn_type", refundTypeField);

        TxnFieldConfig refundAmountField = new TxnFieldConfig();
        refundAmountField.setSource("json");
        refundAmountField.setPath("$.amount");
        refundFields.put("amount", refundAmountField);

        refundConfig.setTransaction(refundFields);

        Map<String, TxnMappingConfig.TxnTypeConfig> mappings = new HashMap<>();
        mappings.put("PAYMENT", paymentConfig);
        mappings.put("REFUND", refundConfig);

        lenient().when(txnMappingConfig.getMappings()).thenReturn(mappings);
    }

    @Test
    void shouldProcessPaymentTransaction() {
        when(repository.insertTransactionDynamic(anyMap())).thenReturn("txn-123");
        doNothing().when(repository).insertTransactionDetailsOnce(anyString(), anyMap());

        TransactionRequest request = new TransactionRequest();
        request.setTxnType("PAYMENT");
        request.setPayload("{\"amount\": 100, \"currency\": \"INR\", \"merchantId\": \"M-1\"}");

        String txnId = service.process(request);

        assertEquals("txn-123", txnId);
        verify(repository).insertTransactionDynamic(anyMap());
    }

    @Test
    void shouldProcessRefundTransaction() {
        when(repository.insertTransactionDynamic(anyMap())).thenReturn("txn-456");

        TransactionRequest request = new TransactionRequest();
        request.setTxnType("REFUND");
        request.setPayload("{\"amount\": 50}");

        String txnId = service.process(request);

        assertEquals("txn-456", txnId);
        verify(repository).insertTransactionDynamic(anyMap());
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.process(null));
    }

    @Test
    void shouldThrowExceptionWhenTxnTypeIsNull() {
        TransactionRequest request = new TransactionRequest();
        request.setPayload("{\"amount\": 100}");

        assertThrows(IllegalArgumentException.class, () -> service.process(request));
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNull() {
        TransactionRequest request = new TransactionRequest();
        request.setTxnType("PAYMENT");

        assertThrows(IllegalArgumentException.class, () -> service.process(request));
    }

    @Test
    void shouldThrowExceptionForUnsupportedTxnType() {
        TransactionRequest request = new TransactionRequest();
        request.setTxnType("INVALID");
        request.setPayload("{\"amount\": 100}");

        assertThrows(IllegalArgumentException.class, () -> service.process(request));
    }
}