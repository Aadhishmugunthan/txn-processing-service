package com.company.txn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String insertTransaction(String txnType, double amount, String currency) {

        String txnId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO transaction (txn_id, txn_type, amount, currency) VALUES (?, ?, ?, ?)",
                txnId,
                txnType,
                amount,
                currency
        );

        return txnId;
    }
}
