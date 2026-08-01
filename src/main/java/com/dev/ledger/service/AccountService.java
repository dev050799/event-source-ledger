package com.dev.ledger.service;

import com.dev.ledger.domain.AccountType;
import com.dev.ledger.domain.Side;
import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.AccountBalance;
import com.dev.ledger.exception.AccountNotFoundException;
import com.dev.ledger.repository.AccountBalanceRepository;
import com.dev.ledger.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;

    public AccountService(AccountRepository accountRepository, AccountBalanceRepository accountBalanceRepository) {
        this.accountRepository = accountRepository;
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account create(String name, AccountType type, String currency, long creditLimit) {
        UUID id = UUID.randomUUID();
        Side normalSide = type.normalSide();
        Account account = accountRepository.saveAndFlush(new Account(id, name, type, normalSide, currency, creditLimit));
        accountBalanceRepository.save(new AccountBalance(id, creditLimit));
        return account;
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
