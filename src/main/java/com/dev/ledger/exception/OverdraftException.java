package com.dev.ledger.exception;

import java.util.UUID;

public final class OverdraftException extends LedgerException {
    public OverdraftException(UUID accountId, long projected, long creditLimit) {
        super("Transaction would overdraw account %s: resulting balance %d < minimum %d"
                .formatted(accountId, projected, -creditLimit));
    }
}
