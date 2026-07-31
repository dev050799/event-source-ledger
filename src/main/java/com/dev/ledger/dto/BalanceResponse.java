package com.dev.ledger.dto;

import java.time.Instant;
import java.util.UUID;

public record BalanceResponse(UUID accountId, long balance, String currency, Instant asOf) {
}
