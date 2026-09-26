package com.dev.ledger.service;

import com.dev.ledger.domain.Money;
import com.dev.ledger.domain.PostTransactionCommand;
import com.dev.ledger.domain.PostTransactionCommand.Leg;
import com.dev.ledger.domain.Side;
import com.dev.ledger.entity.Account;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountService accountService;
    private final ExchangeRateService exchangeRateService;
    private final LedgerService ledgerService;

    public TransferService(AccountService accountService, ExchangeRateService exchangeRateService, LedgerService ledgerService) {
        this.accountService = accountService;
        this.exchangeRateService = exchangeRateService;
        this.ledgerService = ledgerService;
    }

    public UUID transfer(UUID fromAccountId, UUID toAccountId, Money amount, String idempotencyKey,
                         String type, Map<String, Object> metadata) {
        Account from = accountService.get(fromAccountId);
        Account to = accountService.get(toAccountId);


        Side outDirection = from.getNormalSide().opposite();

        List<Leg> legs = new ArrayList<>();
        legs.add(new Leg(from.getId(), outDirection, amount));

        if (from.getCurrency().equals(to.getCurrency())) {
            legs.add(new Leg(to.getId(), outDirection.opposite(), amount));
        } else {
            Money converted = exchangeRateService.convert(amount, to.getCurrency());
            Account sourceClearing = exchangeRateService.clearingAccountFor(from.getCurrency());
            Account destClearing = exchangeRateService.clearingAccountFor(to.getCurrency());

            Side inDirection = to.getNormalSide();
            legs.add(new Leg(sourceClearing.getId(), outDirection.opposite(), amount));
            legs.add(new Leg(destClearing.getId(), inDirection.opposite(), converted));
            legs.add(new Leg(to.getId(), inDirection, converted));
        }

        PostTransactionCommand command = new PostTransactionCommand(idempotencyKey,
                type == null ? "TRANSFER" : type, Instant.now(), legs, metadata, null);
        return ledgerService.post(command);
    }
}