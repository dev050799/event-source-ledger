package com.dev.ledger.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostTransactionCommand(
        String idempotencyKey,
        String type,
        Instant effectiveAt,
        List<Leg> legs,
        Map<String, Object> metadata,
        UUID reverseTransactionId) {

    public record Leg(UUID accountId, Side direction, Money amount) {
    }
}
