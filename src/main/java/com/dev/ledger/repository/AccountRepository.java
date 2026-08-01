package com.dev.ledger.repository;

import com.dev.ledger.domain.AccountType;
import com.dev.ledger.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByTypeAndCurrency(AccountType type, String currency);
}
