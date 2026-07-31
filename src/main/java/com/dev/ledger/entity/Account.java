package com.dev.ledger.entity;

import com.dev.ledger.domain.AccountType;
import com.dev.ledger.domain.Side;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "normal_side", nullable = false, length = 2)
    private Side normalSide;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "credit_limit", nullable = false)
    private long creditLimit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {
    }

    public Account(UUID id, String name, AccountType type, Side normalSide,
                   String currency, long creditLimit) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.normalSide = normalSide;
        this.currency = currency;
        this.creditLimit = creditLimit;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public Side getNormalSide() {
        return normalSide;
    }

    public String getCurrency() {
        return currency;
    }

    public long getCreditLimit() {
        return creditLimit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
