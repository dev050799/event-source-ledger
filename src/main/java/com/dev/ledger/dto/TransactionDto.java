package com.dev.ledger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionDto {

    public record LegRequest(
            @NotNull UUID accountId,
            @NotBlank String direction,
            @Positive long amount,
            @NotBlank @Size(min = 3, max = 3) String currency) {
    }

    public record PostTransactionRequest(
            @NotBlank String type,
            Instant effectiveAt,
            @NotNull @Size(min = 2) @Valid List<LegRequest> legs,
            Map<String, Object> metadata) {
    }

    public record ReverseRequest(String reason) {
    }

    public record TransactionResponse(UUID transactionId, Long sequence) {
    }
}
