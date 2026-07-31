package com.dev.ledger.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_snapshot")
@IdClass(AccountSnapshot.Key.class)
public class AccountSnapshot {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "as_of_sequence")
    private long asOfSequence;

    @Column(name = "as_of_time", nullable = false)
    private Instant asOfTime;

    @Column(nullable = false)
    private long balance;

    @Column(name = "entry_count", nullable = false)
    private long entryCount;

    protected AccountSnapshot() {
    }

    public AccountSnapshot(UUID accountId, long asOfSequence, Instant asOfTime, long balance, long entryCount) {
        this.accountId = accountId;
        this.asOfSequence = asOfSequence;
        this.asOfTime = asOfTime;
        this.balance = balance;
        this.entryCount = entryCount;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public long getAsOfSequence() {
        return asOfSequence;
    }

    public Instant getAsOfTime() {
        return asOfTime;
    }

    public long getBalance() {
        return balance;
    }

    public long getEntryCount() {
        return entryCount;
    }

    public static class Key implements Serializable {
        private UUID accountId;
        private long asOfSequence;

        public Key() {
        }

        public Key(UUID accountId, long asOfSequence) {
            this.accountId = accountId;
            this.asOfSequence = asOfSequence;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return asOfSequence == k.asOfSequence && Objects.equals(accountId, k.accountId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, asOfSequence);
        }

    }
}
