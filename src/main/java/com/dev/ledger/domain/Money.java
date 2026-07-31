package com.dev.ledger.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(long minorUnits, String currency) {

    public Money {
        Objects.requireNonNull(currency, "currency");
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO code: " + currency);
        }
    }

    public static Money of(long minorUnits, String currency) {
        return new Money(minorUnits, currency);
    }

    public boolean isPositive() {
        return minorUnits > 0;
    }

    public BigDecimal toMajor() {
        int fraction = Currency.getInstance(currency).getDefaultFractionDigits();
        return BigDecimal.valueOf(minorUnits, Math.max(fraction, 0));
    }

    @Override
    public String toString() {
        return toMajor().toPlainString() + " " + currency;
    }
}
