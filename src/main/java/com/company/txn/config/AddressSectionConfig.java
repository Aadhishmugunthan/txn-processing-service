package com.company.txn.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddressSectionConfig {
    private List<AddressFieldDefinition> definitions;

    @Getter
    @Setter
    public static class AddressFieldDefinition {
        private String jsonPath;     // $.billingAddress
        private String addressType;  // BILLING
    }
}
