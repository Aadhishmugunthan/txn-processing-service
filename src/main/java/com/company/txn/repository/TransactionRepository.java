package com.company.txn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.StringJoiner;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String insertTransactionDynamic(Map<String, Object> columns) {

        String sql = "INSERT INTO transaction (txn_id, txn_type, amount, currency) VALUES (?, ?, ?, ?)";

        String txnId = (String) columns.get("txn_id");

        jdbcTemplate.update(
                sql,
                columns.get("txn_id"),
                columns.get("txn_type"),
                columns.get("amount"),
                columns.get("currency")
        );

        return txnId;
    }

    public void insertTransactionDetail(
            String txnId,
            String key,
            Object value
    ) {
        jdbcTemplate.update(
                "INSERT INTO transaction_details (txn_id, key, value) VALUES (?, ?, ?)",
                txnId,
                key,
                value != null ? value.toString() : null
        );
    }

    public void insertTransactionAddress(
            String addressId,
            String txnId,
            String addressType,
            String fieldKey,
            String fieldValue,
            String country
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO transaction_address
                (address_id, txn_id, address_type, field_key, field_value, country)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                addressId,
                txnId,
                addressType,
                fieldKey,
                fieldValue,
                country
        );
    }

}
