package com.dev.ledger.exception;

import java.util.UUID;

public final class AccountNotFoundException extends LedgerException {
    public AccountNotFoundException(UUID accountId) {
        super("Account not found: " + accountId);
    }
}
