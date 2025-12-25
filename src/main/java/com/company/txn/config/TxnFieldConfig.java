package com.company.txn.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxnFieldConfig {
    private String source; // json | constant | generated
    private String path;   // $.amount, $.billing.city
    private boolean required;
    private String value; // for constant
}
