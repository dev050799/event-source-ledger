package com.dev.ledger.service;

import com.dev.ledger.domain.PostTransactionCommand;
import com.dev.ledger.domain.PostTransactionCommand.Leg;
import com.dev.ledger.domain.Side;
import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.AccountBalance;
import com.dev.ledger.entity.Entry;
import com.dev.ledger.entity.LedgerTransaction;
import com.dev.ledger.exception.*;
import com.dev.ledger.repository.AccountBalanceRepository;
import com.dev.ledger.repository.AccountRepository;
import com.dev.ledger.repository.EntryRepository;
import com.dev.ledger.repository.LedgerTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LedgerWriter {

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final EntryRepository entryRepository;
    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;

    public LedgerWriter(LedgerTransactionRepository ledgerTransactionRepository, EntryRepository entryRepository,
                        AccountRepository accountRepository, AccountBalanceRepository accountBalanceRepository) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public UUID write(PostTransactionCommand command) {
        if (command.idempotencyKey() != null) {
            Optional<LedgerTransaction> existing = ledgerTransactionRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return existing.get().getId();
            }
        }

        validateBalance(command.legs());

        List<UUID> accountIds = command.legs().stream()
                .map(Leg::accountId).distinct().sorted().toList();

        Map<UUID, AccountBalance> balances = new LinkedHashMap<>();
        for (UUID id : accountIds) {
            balances.put(id, accountBalanceRepository.lockByAccountId(id)
                    .orElseThrow(() -> new AccountNotFoundException(id)));
        }

        Map<UUID, Account> accounts = new HashMap<>();
        for (UUID id : accountIds) {
            accounts.put(id, accountRepository.findById(id)
                    .orElseThrow(() -> new AccountNotFoundException(id)));
        }

        Map<UUID, Long> deltas = new HashMap<>();
        for (Leg leg : command.legs()) {
            Account acct = accounts.get(leg.accountId());
            if (!acct.getCurrency().equals((leg.amount().currency()))) {
                throw new CurrencyMismatchException(acct.getId(), acct.getCurrency(), leg.amount().currency());
            }
            long delta = signedDelta(acct.getNormalSide(), leg.direction(), leg.amount().minorUnits());
            deltas.merge(leg.accountId(), delta, Long::sum);
        }
        for (Map.Entry<UUID, Long> e : deltas.entrySet()) {
            AccountBalance accountBalance = balances.get(e.getKey());
            long projected = accountBalance.getBalance() + e.getValue();
            if (projected < -accountBalance.getCreditLimit()) {
                throw new OverdraftException(e.getKey(), projected, accountBalance.getCreditLimit());
            }
        }

        LedgerTransaction txn = ledgerTransactionRepository.saveAndFlush(new LedgerTransaction(
                UUID.randomUUID(), command.type(), command.effectiveAt(), command.idempotencyKey(),
                command.reverseTransactionId(), command.metadata()));

        for (Leg leg : command.legs()) {
            entryRepository.save(new Entry(UUID.randomUUID(), txn, leg.accountId(),
                    leg.direction(), leg.amount().minorUnits(), leg.amount().currency()));
        }

        for (Map.Entry<UUID, Long> e : deltas.entrySet()) {
            balances.get(e.getKey()).apply(e.getValue(), txn.getSequence());
        }

        return txn.getId();
    }

    static long signedDelta(Side normalSide, Side legSide, long amount) {
        return legSide == normalSide ? amount : -amount;
    }

    static void validateBalance(List<Leg> legs) {
        if (legs == null || legs.size() < 2) {
            throw new InvalidTransactionException("A transaction needs at least two legs");
        }
        Map<String, Long> netByCurrency = new HashMap<>();
        for (Leg leg : legs) {
            if (!leg.amount().isPositive()) {
                throw new InvalidTransactionException("Leg amount must be positive");
            }
            long signed = leg.direction() == Side.DEBIT ?
                    leg.amount().minorUnits() : -leg.amount().minorUnits();
            netByCurrency.merge(leg.amount().currency(), signed, Long::sum);
        }
        netByCurrency.forEach((ccy, net) -> {
            if (net != 0) {
                throw new UnbalancedTransactionException(ccy, net);
            }
        });
    }
}
