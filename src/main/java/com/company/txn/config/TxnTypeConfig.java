package com.company.txn.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TxnTypeConfig {

    private Map<String, TxnFieldConfig> transaction;
    private List<TxnDetailConfig> transactionDetails;
    private AddressSectionConfig addresses;
    private StatusConfig status;

    @Getter
    @Setter
    public static class StatusConfig {
        private InitialStatus initial;

        @Getter
        @Setter
        public static class InitialStatus {
            private String current_status;
            private String remarks;
        }
    }
}
