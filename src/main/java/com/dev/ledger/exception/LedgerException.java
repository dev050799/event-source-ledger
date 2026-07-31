package com.dev.ledger.exception;

public abstract class LedgerException extends RuntimeException {
    protected LedgerException(String message) {
        super(message);
    }
}
