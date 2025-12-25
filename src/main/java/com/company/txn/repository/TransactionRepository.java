package com.company.txn.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertTransactionDynamic(Map<String, Object> columns) {
        // Build dynamic SQL based on the columns map
        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : columns.entrySet()) {
            columnNames.add(entry.getKey());
            placeholders.add("?");
            values.add(entry.getValue());
        }

        String sql = String.format(
                "INSERT INTO TRANSACTION (%s) VALUES (%s)",
                String.join(", ", columnNames),
                String.join(", ", placeholders)
        );

        jdbcTemplate.update(sql, values.toArray());
    }

    public void insertTransactionDetailsOnce(String txnId, Map<String, Object> detailColumns) {
        if (detailColumns == null || detailColumns.isEmpty()) {
            return;
        }

        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        // Add TXN_ID first
        columnNames.add("TXN_ID");
        placeholders.add("?");
        values.add(txnId);

        // Add other columns
        for (Map.Entry<String, Object> entry : detailColumns.entrySet()) {
            columnNames.add(entry.getKey());
            placeholders.add("?");
            values.add(entry.getValue());
        }

        String sql = String.format(
                "INSERT INTO TRANSACTION_DETAILS (%s) VALUES (%s)",
                String.join(", ", columnNames),
                String.join(", ", placeholders)
        );

        jdbcTemplate.update(sql, values.toArray());
    }

    public void insertTransactionAddress(
            String addressId,
            String txnId,
            String addressType,
            String line1,
            String city,
            String country) {

        String sql = """
            INSERT INTO TRANSACTION_ADDRESS
            (ADDRESS_ID, TXN_ID, ADDRESS_TYPE, LINE1, CITY, COUNTRY)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql, addressId, txnId, addressType, line1, city, country);
    }

    public void insertTransactionStatus(
            String statusId,
            String txnId,
            String currentStatus,
            String remarks) {

        String sql = """
            INSERT INTO TRANSACTION_STATUS
            (STATUS_ID, TXN_ID, CURRENT_STATUS, REMARKS)
            VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql, statusId, txnId, currentStatus, remarks);
    }
}