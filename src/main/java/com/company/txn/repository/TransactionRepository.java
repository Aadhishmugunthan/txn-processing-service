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

        String sql = """
        INSERT INTO TRANSACTION
        (TXN_ID, TXN_TYPE, AMOUNT, CURRENCY, SOURCE_SYSTEM)
        VALUES (?, ?, ?, ?, ?)
        """;

        String txnId = (String) columns.get("txn_id");

        jdbcTemplate.update(
                sql,
                columns.get("txn_id"),
                columns.get("txn_type"),
                columns.get("amount"),
                columns.get("currency"),
                columns.get("source_system") // NEW
        );

        return txnId;
    }

    public void insertTransactionDetailsOnce(
            String txnId,
            Map<String, Object> columns
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO TRANSACTION_DETAILS (TXN_ID, CUSTOMER_ID, CHANNEL)
                VALUES (?, ?, ?)
                """,
                txnId,
                columns.get("CUSTOMER_ID"),
                columns.get("CHANNEL")
        );
    }


    public void insertTransactionAddress(
            String addressId,
            String txnId,
            String addressType,
            String line1,
            String city,
            String country
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO transaction_address
                (address_id, txn_id, address_type, line1, city, country)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                addressId,
                txnId,
                addressType,
                line1,
                city,
                country
        );
    }

}
