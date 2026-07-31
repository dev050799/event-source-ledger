package com.dev.ledger.exception;

public final class InvalidTransactionException extends LedgerException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
