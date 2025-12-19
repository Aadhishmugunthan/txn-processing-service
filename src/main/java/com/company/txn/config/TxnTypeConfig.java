package com.company.txn.config;

import java.util.List;
import java.util.Map;

public class TxnTypeConfig {

    private Map<String, TxnFieldConfig> transaction;
    private List<TxnDetailConfig> transaction_details;
    private AddressSectionConfig addresses;

    public Map<String, TxnFieldConfig> getTransaction() {
        return transaction;
    }

    public void setTransaction(Map<String, TxnFieldConfig> transaction) {
        this.transaction = transaction;
    }

    public List<TxnDetailConfig> getTransaction_details() {
        return transaction_details;
    }

    public void setTransaction_details(List<TxnDetailConfig> transaction_details) {
        this.transaction_details = transaction_details;
    }

    public AddressSectionConfig getAddresses() {
        return addresses;
    }

    public void setAddresses(AddressSectionConfig addresses) {
        this.addresses = addresses;
    }
}
