package com.dev.ledger.service;

import com.dev.ledger.domain.AuditResult;
import com.dev.ledger.domain.Money;
import com.dev.ledger.domain.Side;
import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.Entry;
import com.dev.ledger.entity.LedgerTransaction;
import com.dev.ledger.exception.AccountNotFoundException;
import com.dev.ledger.repository.AccountRepository;
import com.dev.ledger.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    public AuditService(AccountRepository accountRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
    }

    public AuditResult audit(UUID accountId, Instant from, Instant to) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        Side normalSide = account.getNormalSide();

        long opening = entryRepository.foldBefore(accountId, normalSide, from);
        List<Entry> legs = entryRepository.findForAudit(accountId, from, to);

        Set<UUID> txnIds = legs.stream()
                .map(e -> e.getTransaction().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<UUID, List<Entry>> siblings = txnIds.isEmpty() ? Map.of() :
                entryRepository.findByTransactionIds(txnIds).stream()
                        .collect(Collectors.groupingBy(e -> e.getTransaction().getId()));

        List<AuditResult.Step> steps = new ArrayList<>();
        long running = opening;

        for (Entry leg : legs) {
            LedgerTransaction txn = leg.getTransaction();
            long delta = LedgerWriter.signedDelta(normalSide, leg.getDirection(), leg.getAmount());
            running += delta;

            List<AuditResult.CounterParty> counterParties = siblings.getOrDefault(txn.getId(), List.of())
                    .stream().filter(s -> !s.getId().equals(leg.getId()))
                    .map(s -> new AuditResult.CounterParty(s.getAccountId(), s.getDirection(),
                            s.getAmount(), s.getCurrency())).toList();

            steps.add(new AuditResult.Step(
                    txn.getSequence(), txn.getId(), txn.getType(), txn.getEffectiveAt(),
                    txn.getRecordedAt(), leg.getDirection(), leg.getAmount(), running,
                    txn.getReverseTransactionId(), counterParties, explain(leg, delta, counterParties)));
        }
        return new AuditResult(accountId, account.getCurrency(), opening, running, steps);
    }

    public String explain(Entry leg, long delta, List<AuditResult.CounterParty> counterParties) {
        String verb = delta >= 0 ? "Increased" : "Decreased";
        String money = new Money(leg.getAmount(), leg.getCurrency()).toString();
        String to = counterParties.isEmpty() ? "" : " (counterparty " + counterParties.get(0).accountId() + ")";
        return "%s by %s via a %s leg%s.".formatted(
                verb, money, leg.getDirection() == Side.DEBIT ? "debit" : "credit", to);

    }
}
