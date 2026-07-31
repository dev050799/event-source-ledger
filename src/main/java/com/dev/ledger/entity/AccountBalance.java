package com.dev.ledger.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "account_balance")
public class AccountBalance {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false)
    private long balance;

    @Column(name = "credit_limit", nullable = false)
    private long creditLimit;

    @Column(name = "last_sequence", nullable = false)
    private long lastSequence;

    @Version
    private long version;

    protected AccountBalance() {
    }

    public AccountBalance(UUID accountId, long creditLimit) {
        this.accountId = accountId;
        this.balance = 0;
        this.creditLimit = creditLimit;
        this.lastSequence = 0;
    }

    public void apply(long delta, long sequence) {
        this.balance += delta;
        this.lastSequence = sequence;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public long getBalance() {
        return balance;
    }

    public long getCreditLimit() {
        return creditLimit;
    }

    public long getLastSequence() {
        return lastSequence;
    }
}
