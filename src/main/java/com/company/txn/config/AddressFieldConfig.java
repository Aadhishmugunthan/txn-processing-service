package com.company.txn.config;

import java.util.Map;

public class AddressFieldConfig {

    private String addressType;
    private String jsonPath;
    private Map<String, TxnFieldConfig> fields;

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public Map<String, TxnFieldConfig> getFields() {
        return fields;
    }

    public void setFields(Map<String, TxnFieldConfig> fields) {
        this.fields = fields;
    }
}
