package com.dev.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "exchange_rate")
public class ExchangeRate {

    @Id
    private UUID id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(nullable = false, precision = 28, scale = 10)
    private BigDecimal rate;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;

    protected ExchangeRate() {
    }

    public ExchangeRate(UUID id, String baseCurrency, String quoteCurrency, BigDecimal rate, Instant asOf) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.rate = rate;
        this.asOf = asOf;
    }

    public UUID getId() {
        return id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public Instant getAsOf() {
        return asOf;
    }
}