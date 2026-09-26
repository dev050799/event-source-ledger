package com.dev.ledger.service;

import com.dev.ledger.entity.Account;
import com.dev.ledger.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(SnapshotScheduler.class);

    private final AccountRepository accountRepository;
    private final SnapshotService snapshotService;

    public SnapshotScheduler(AccountRepository accountRepository, SnapshotService snapshotService) {
        this.accountRepository = accountRepository;
        this.snapshotService = snapshotService;
    }

    @Scheduled(fixedDelayString = "${ledger.snapshot.interval-ms:300000}",
            initialDelayString = "${ledger.snapshot.initial-delay-ms:300000}")
    public void snapshotAllAccounts() {
        for (Account account : accountRepository.findAll()) {
            snapshotService.snapshotIfChanged(account.getId())
                    .ifPresent(s -> log.debug("Snapshotted account {} at sequence {}", s.getAccountId(), s.getAsOfSequence()));
        }
    }
}