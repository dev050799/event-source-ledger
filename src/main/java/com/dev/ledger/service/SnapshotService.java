package com.dev.ledger.service;

import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.AccountBalance;
import com.dev.ledger.entity.AccountSnapshot;
import com.dev.ledger.exception.AccountNotFoundException;
import com.dev.ledger.repository.AccountBalanceRepository;
import com.dev.ledger.repository.AccountRepository;
import com.dev.ledger.repository.AccountSnapshotRepository;
import com.dev.ledger.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SnapshotService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final AccountSnapshotRepository accountSnapshotRepository;
    private final EntryRepository entryRepository;

    public SnapshotService(AccountRepository accountRepository, AccountBalanceRepository accountBalanceRepository,
                           AccountSnapshotRepository accountSnapshotRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.accountSnapshotRepository = accountSnapshotRepository;
        this.entryRepository = entryRepository;
    }

    @Transactional
    public AccountSnapshot snapshot(UUID accountId) {
        AccountBalance bal = accountBalanceRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        long count = entryRepository.countByAccountId(accountId);
        return accountSnapshotRepository.save(new AccountSnapshot(accountId, bal.getLastSequence(), Instant.now(),
                bal.getBalance(), count));
    }

    @Transactional(readOnly = true)
    public long currentBalanceFromSnapshot(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return accountSnapshotRepository.findLatest(accountId)
                .map(s -> s.getBalance()
                        + entryRepository.foldAfterSequence(accountId, account.getNormalSide(), s.getAsOfSequence()))
                .orElseGet(() -> entryRepository.foldAfterSequence(accountId, account.getNormalSide(), 0L));
    }
}
