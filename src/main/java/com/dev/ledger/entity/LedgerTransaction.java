package com.dev.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ledger_transaction")
public class LedgerTransaction {

    @Id
    private UUID id;

    @Generated(event = EventType.INSERT)
    @Column(name = "sequence", insertable = false, updatable = false)
    private long sequence;

    @Column(nullable = false)
    private String type;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "reverse_transaction_id")
    private UUID reverseTransactionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata = new HashMap<>();

    protected LedgerTransaction() {
    }

    public LedgerTransaction(UUID id, String type, Instant effectiveAt, String idempotencyKey, UUID reverseTransactionId, Map<String, Object> metadata) {
        this.id = id;
        this.type = type;
        this.effectiveAt = effectiveAt;
        this.recordedAt = Instant.now();
        this.idempotencyKey = idempotencyKey;
        this.reverseTransactionId = reverseTransactionId;
        this.metadata = metadata == null ? new HashMap<>() : metadata;
    }

    public UUID getId() {
        return id;
    }

    public long getSequence() {
        return sequence;
    }

    public String getType() {
        return type;
    }

    public Instant getEffectiveAt() {
        return effectiveAt;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getReverseTransactionId() {
        return reverseTransactionId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
