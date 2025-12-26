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

    public boolean transactionExists(String txnId) {
        String sql = "SELECT COUNT(*) FROM TRANSACTION WHERE TXN_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, txnId);
        return count != null && count > 0;
    }

    public void insertTransactionDynamic(Map<String, Object> columns) {
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

    public void updateTransactionDynamic(String txnId, Map<String, Object> columns) {
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : columns.entrySet()) {
            if (!entry.getKey().equals("TXN_ID")) {
                setClauses.add(entry.getKey() + " = ?");
                values.add(entry.getValue());
            }
        }

        values.add(txnId);

        String sql = String.format(
                "UPDATE TRANSACTION SET %s WHERE TXN_ID = ?",
                String.join(", ", setClauses)
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

        columnNames.add("TXN_ID");
        placeholders.add("?");
        values.add(txnId);

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

    public void updateTransactionDetailsOnce(String txnId, Map<String, Object> detailColumns) {
        if (detailColumns == null || detailColumns.isEmpty()) {
            return;
        }

        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : detailColumns.entrySet()) {
            setClauses.add(entry.getKey() + " = ?");
            values.add(entry.getValue());
        }

        values.add(txnId);

        String sql = String.format(
                "UPDATE TRANSACTION_DETAILS SET %s WHERE TXN_ID = ?",
                String.join(", ", setClauses)
        );

        jdbcTemplate.update(sql, values.toArray());
    }

    public void deleteTransactionAddresses(String txnId) {
        String sql = "DELETE FROM TRANSACTION_ADDRESS WHERE TXN_ID = ?";
        jdbcTemplate.update(sql, txnId);
    }

    public void deleteTransactionStatus(String txnId) {
        String sql = "DELETE FROM TRANSACTION_STATUS WHERE TXN_ID = ?";
        jdbcTemplate.update(sql, txnId);
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