package com.dev.ledger.exception;

public final class UnbalancedTransactionException extends LedgerException {
    public UnbalancedTransactionException(String currency, long net) {
        super("Transaction does not balance for %s: debits - credits = %d (must be 0)"
                .formatted(currency, net));
    }
}
