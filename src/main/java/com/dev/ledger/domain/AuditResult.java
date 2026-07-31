package com.dev.ledger.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditResult(
        UUID accountId,
        String currency,
        long openingBalance,
        long closingBalance,
        List<Step> steps) {

    public record Step(
            long sequence,
            UUID transactionId,
            String transactionType,
            Instant effectiveAt,
            Instant recordedAt,
            Side direction,
            long amount,
            long runningBalance,
            UUID reverse,
            List<CounterParty> counterParties,
            String explanation
    ) {
    }

    public record CounterParty(
            UUID accountId,
            Side direction,
            long amount,
            String currency
    ) {
    }
}
