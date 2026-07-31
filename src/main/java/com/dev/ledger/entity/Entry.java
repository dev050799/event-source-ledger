package com.dev.ledger.entity;

import com.dev.ledger.domain.Side;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "entry")
public class Entry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, length = 2)
    private Side direction;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected Entry() {
    }

    public Entry(UUID id, LedgerTransaction transaction, UUID accountId, Side direction, long amount, String currency) {
        this.id = id;
        this.transaction = transaction;
        this.accountId = accountId;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public LedgerTransaction getTransaction() {
        return transaction;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Side getDirection() {
        return direction;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
