package com.company.txn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "txn")
public class TxnMappingConfig {

    private Map<String, TxnTypeConfig> mappings;

    public Map<String, TxnTypeConfig> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, TxnTypeConfig> mappings) {
        this.mappings = mappings;
    }
}
