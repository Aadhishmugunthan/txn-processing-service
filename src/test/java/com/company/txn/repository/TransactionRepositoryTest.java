package com.company.txn.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TransactionRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TransactionRepository repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldInsertTransactionDynamic() {

        Map<String, Object> columns = new HashMap<>();
        columns.put("TXN_ID", "TXN001");
        columns.put("TXN_TYPE", "PAYMENT");

        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);

        // METHOD RETURNS VOID — JUST CALL IT
        repository.insertTransactionDynamic(columns);

        // VERIFY IT WAS CALLED
        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @Test
    void shouldReturnTrue_whenTransactionExists() {

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenReturn(1);

        boolean exists = repository.transactionExists("TXN001");

        assert exists;
    }

    @Test
    void shouldReturnFalse_whenTransactionDoesNotExist() {

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any()))
                .thenReturn(0);

        boolean exists = repository.transactionExists("TXN002");

        assert !exists;
    }
}
