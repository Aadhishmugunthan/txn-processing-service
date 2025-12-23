package com.company.txn.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TransactionRepository repository;

    @Test
    void shouldInsertTransactionDynamic() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> columns = new HashMap<>();
        columns.put("txn_id", "txn-1");
        columns.put("txn_type", "PAYMENT");

        String txnId = repository.insertTransactionDynamic(columns);

        assertEquals("txn-1", txnId);
        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldInsertTransactionDetails() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        Map<String, Object> details = new HashMap<>();
        details.put("merchant_id", "MERCH-123");

        repository.insertTransactionDetailsOnce("txn-1", details);

        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldInsertTransactionAddress() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        repository.insertTransactionAddress("addr-1", "txn-1", "BILLING", "123 St", "Mumbai", "INDIA");

        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }
}