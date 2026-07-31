package com.dev.ledger.service;

import com.dev.ledger.entity.Account;
import com.dev.ledger.exception.AccountNotFoundException;
import com.dev.ledger.repository.AccountBalanceRepository;
import com.dev.ledger.repository.AccountRepository;
import com.dev.ledger.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BalanceQueryService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final EntryRepository entryRepository;

    public BalanceQueryService(AccountRepository accountRepository, AccountBalanceRepository accountBalanceRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
        this.entryRepository = entryRepository;
    }

    public long currentBalance(UUID accountId) {
        return accountBalanceRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .getBalance();
    }

    public long balanceAsOf(UUID accountId, Instant asOf) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return entryRepository.foldAsOf(accountId, account.getNormalSide(), asOf);
    }

    public String currencyOf(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId))
                .getCurrency();
    }
}
