package com.dev.ledger.repository;

import com.dev.ledger.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    @Query("""
            select r from ExchangeRate r
            where r.baseCurrency = :base and r.quoteCurrency = :quote
            order by r.asOf desc limit 1
            """)
    Optional<ExchangeRate> findLatest(@Param("base") String base, @Param("quote") String quote);
}