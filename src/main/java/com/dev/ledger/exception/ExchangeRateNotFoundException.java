package com.dev.ledger.exception;

public final class ExchangeRateNotFoundException extends LedgerException {
    public ExchangeRateNotFoundException(String baseCurrency, String quoteCurrency) {
        super("No exchange rate available for %s -> %s".formatted(baseCurrency, quoteCurrency));
    }
}