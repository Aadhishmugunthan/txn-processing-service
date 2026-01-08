package com.company.txn.exception;

public class DroolsExecutionException extends RuntimeException {

    public DroolsExecutionException(String message) {
        super(message);
    }

    public DroolsExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
