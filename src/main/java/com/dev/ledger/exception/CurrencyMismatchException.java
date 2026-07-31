package com.dev.ledger.exception;

import java.util.UUID;

public class CurrencyMismatchException extends LedgerException {
    public CurrencyMismatchException(UUID accountId, String accountCurrency, String legCurrency) {
        super("Leg currency %s does not match account %s currency %s.".formatted(legCurrency, accountId, accountCurrency));
    }
}
