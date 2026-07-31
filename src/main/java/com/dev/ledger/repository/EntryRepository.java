package com.dev.ledger.repository;

import com.dev.ledger.domain.Side;
import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EntryRepository extends JpaRepository<Entry, UUID> {

    @Query("""
            select coalesce(sum(case when e.direction = :normalSide then e.amount else -e.amount end), 0)
            from Entry e where e.accountId = :accountId
            and e.transaction.effectiveAt <= :asOf
            """)
    long foldAsOf(@Param("accountId") UUID accountId, @Param("normalSide") Side normalSide,
                  @Param("asOf") Instant asOf);

    @Query("""
            select coalesce(sum(case when e.direction = :normalSide then e.amount else -e.amount end), 0)
            from Entry e where e.accountId = :accountId
            and e.transaction.effectiveAt < :before
            """)
    long foldBefore(@Param("accountId") UUID accountId, @Param("normalSide") Side normalSide,
                    @Param("before") Instant before);

    @Query("""
            select coalesce(sum(case when e.direction = :normalSide then e.amount else -e.amount end), 0)
            from Entry e where e.accountId = :accountId
            and e.transaction.sequence > :afterSequence
            """)
    long foldAfterSequence(@Param("accountId") UUID accountId, @Param("normalSide") Side normalSide,
                           @Param("afterSequence") long afterSequence);

    long countByAccountId(UUID accountId);

    @Query("""
            select e from Entry e where e.accountId = :accountId
            and e.transaction.effectiveAt between :from and :to
            order by e.transaction.sequence
            """)
    List<Entry> findForAudit(@Param("accountId") UUID accountId, @Param("from") Instant from,
                             @Param("from") Instant to);

    @Query("select e from Entry e where e.transaction.id in :transactionIds")
    List<Entry> findByTransactionIds(@Param("transactionIds") Collection<UUID> transactionIds);

    @Query("""
            select coalesce(sum(case when e.direction = :debit then e.amount else -e.amount end), 0)
            from Entry e where e.currency = :currency
            """)
    long signedSumForCurrency(@Param("debit") Side debit, @Param("currency") String normalSide);
}
