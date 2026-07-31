package com.dev.ledger.repository;

import com.dev.ledger.entity.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, UUID> {

    @Query("""
            select s from AccountSnapshot s where s.accountId = :accountId
            order by s.asOfSequence desc limit 1
            """)
    Optional<AccountSnapshot> findLatest(@Param("accountId") UUID accountId);
}
