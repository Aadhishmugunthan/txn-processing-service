package com.company.txn.model;

public class TransactionRequest {

    private String txnType;
    private String payload;  // <-- This MUST be String, not Map or Object

    public TransactionRequest() {
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}