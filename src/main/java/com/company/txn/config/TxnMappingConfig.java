package com.company.txn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "txn-mapping")
public class TxnMappingConfig extends HashMap<String, TxnMappingConfig.TxnTypeConfig> {

    public static class TxnTypeConfig {
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
