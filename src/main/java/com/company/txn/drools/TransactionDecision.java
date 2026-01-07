package com.company.txn.drools;

public class TransactionDecision {

    private String operation;     // "A" or "I"
    private boolean processTxn;   // result from rules

    public TransactionDecision() {
    }

    public TransactionDecision(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public boolean isProcessTxn() {
        return processTxn;
    }

    public void setProcessTxn(boolean processTxn) {
        this.processTxn = processTxn;
    }
}
