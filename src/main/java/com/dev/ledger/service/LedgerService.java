package com.dev.ledger.service;

import com.dev.ledger.domain.Money;
import com.dev.ledger.domain.PostTransactionCommand;
import com.dev.ledger.domain.PostTransactionCommand.Leg;
import com.dev.ledger.dto.TransactionDto.TransactionResponse;
import com.dev.ledger.entity.Entry;
import com.dev.ledger.entity.LedgerTransaction;
import com.dev.ledger.exception.TransactionNotFoundException;
import com.dev.ledger.repository.EntryRepository;
import com.dev.ledger.repository.LedgerTransactionRepository;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerWriter ledgerWriter;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final EntryRepository entryRepository;

    public LedgerService(LedgerWriter ledgerWriter, LedgerTransactionRepository ledgerTransactionRepository, EntryRepository entryRepository) {
        this.ledgerWriter = ledgerWriter;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.entryRepository = entryRepository;
    }

    @Retryable(retryFor = ConcurrencyFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 2, multiplier = 2, maxDelay = 250))
    public UUID post(PostTransactionCommand command) {
        try {
            return ledgerWriter.write(command);
        } catch (DataIntegrityViolationException dex) {
            if (command.idempotencyKey() != null) {
                return ledgerTransactionRepository.findByIdempotencyKey(command.idempotencyKey())
                        .map(LedgerTransaction::getId)
                        .orElseThrow(() -> dex);
            }
            throw dex;
        }
    }

    public UUID reverse(UUID originalTxnId, String idempotencyKey, String reason) {
        LedgerTransaction original = ledgerTransactionRepository.findById(originalTxnId)
                .orElseThrow(() -> new TransactionNotFoundException(originalTxnId));

        List<Entry> originalLegs = entryRepository.findByTransactionIds(List.of(originalTxnId));

        List<Leg> reversedLegs = originalLegs.stream()
                .map((e -> new Leg(e.getAccountId(), e.getDirection().opposite(),
                        new Money(e.getAmount(), e.getCurrency()))))
                .toList();

        PostTransactionCommand command = new PostTransactionCommand(idempotencyKey,
                "REVERSAL", Instant.now(), reversedLegs, Map.of("reason", reason == null ? "" : reason),
                originalTxnId);

        return post(command);
    }

    public TransactionResponse transactionResponse(UUID id) {
        LedgerTransaction txn = ledgerTransactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return new TransactionResponse(txn.getId(), txn.getSequence());
    }
}
