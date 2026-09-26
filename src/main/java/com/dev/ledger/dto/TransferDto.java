package com.dev.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public class TransferDto {

    public record TransferRequest(
            @NotNull UUID fromAccountId,
            @NotNull UUID toAccountId,
            @Positive long amount,
            @NotBlank @Size(min = 3, max = 3) String currency,
            String type,
            Map<String, Object> metadata) {
    }
}