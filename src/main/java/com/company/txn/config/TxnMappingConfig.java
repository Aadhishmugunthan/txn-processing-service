package com.company.txn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "txn-mapping")
public class TxnMappingConfig {

    private Map<String, TxnTypeConfig> mappings;

    public Map<String, TxnTypeConfig> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, TxnTypeConfig> mappings) {
        this.mappings = mappings;
    }

    public static class TxnTypeConfig {

        private Map<String, TxnFieldConfig> transaction;
        private List<TxnDetailConfig> transactionDetails;
        private AddressSectionConfig addresses;

        public Map<String, TxnFieldConfig> getTransaction() {
            return transaction;
        }

        public void setTransaction(Map<String, TxnFieldConfig> transaction) {
            this.transaction = transaction;
        }

        public List<TxnDetailConfig> getTransactionDetails() {
            return transactionDetails;
        }

        public void setTransactionDetails(List<TxnDetailConfig> transactionDetails) {
            this.transactionDetails = transactionDetails;
        }

        public AddressSectionConfig getAddresses() {
            return addresses;
        }

        public void setAddresses(AddressSectionConfig addresses) {
            this.addresses = addresses;
        }
    }
}
