package com.company.txn.config;

import com.jayway.jsonpath.JsonPath;

import java.util.UUID;

public class FieldValueResolver {

    public static Object resolve(TxnFieldConfig config, String payloadJson) {

        Object value;

        switch (config.getSource()) {

            case "generated":
                value = UUID.randomUUID().toString();
                break;

            case "constant":
                value = config.getValue();
                break;

            case "json":
                value = JsonPath.read(payloadJson, config.getPath());
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported source type: " + config.getSource()
                );
        }

        if (config.isRequired() && value == null) {
            throw new IllegalArgumentException("Required field missing");
        }

        return value;
    }
}
