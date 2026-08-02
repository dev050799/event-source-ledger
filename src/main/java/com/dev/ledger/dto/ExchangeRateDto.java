package com.dev.ledger.dto;

import com.dev.ledger.entity.ExchangeRate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class ExchangeRateDto {

    public record SetRateRequest(
            @NotBlank @Size(min = 3, max = 3) String baseCurrency,
            @NotBlank @Size(min = 3, max = 3) String quoteCurrency,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal rate) {
    }

    public record RateResponse(String baseCurrency, String quoteCurrency, BigDecimal rate, Instant asOf) {
        public static RateResponse from(ExchangeRate r) {
            return new RateResponse(r.getBaseCurrency(), r.getQuoteCurrency(), r.getRate(), r.getAsOf());
        }
    }
}