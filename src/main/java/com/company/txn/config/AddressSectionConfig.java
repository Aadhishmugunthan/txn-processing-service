package com.company.txn.config;

import java.util.List;

public class AddressSectionConfig {

    private int expectedSize;
    private List<AddressFieldConfig> definitions;

    public int getExpectedSize() {
        return expectedSize;
    }

    public void setExpectedSize(int expectedSize) {
        this.expectedSize = expectedSize;
    }

    public List<AddressFieldConfig> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<AddressFieldConfig> definitions) {
        this.definitions = definitions;
    }
}
