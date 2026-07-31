package com.dev.ledger.dto;

import com.dev.ledger.domain.AccountType;
import com.dev.ledger.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AccountDto {

    public record CreateAccountRequest(
            @NotBlank String name,
            @NotNull AccountType type,
            @NotBlank @Size(min = 3, max = 3) String currency,
            @PositiveOrZero long creditLimit) {
    }

    public record AccountResponse(
            UUID id, String name, AccountType type, String normalSide,
            String currency, long creditLimit) {

        public static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getName(), a.getType(),
                    a.getNormalSide().code(), a.getCurrency(), a.getCreditLimit());
        }
    }
}
