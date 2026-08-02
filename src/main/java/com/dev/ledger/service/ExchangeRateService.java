package com.dev.ledger.service;

import com.dev.ledger.domain.AccountType;
import com.dev.ledger.domain.Money;
import com.dev.ledger.entity.Account;
import com.dev.ledger.entity.ExchangeRate;
import com.dev.ledger.exception.ExchangeRateNotFoundException;
import com.dev.ledger.repository.AccountRepository;
import com.dev.ledger.repository.ExchangeRateRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Service
public class ExchangeRateService {

    private static final int RATE_SCALE = 10;

    private final ExchangeRateRepository exchangeRateRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, AccountRepository accountRepository,
                                AccountService accountService) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Transactional
    public ExchangeRate setRate(String baseCurrency, String quoteCurrency, BigDecimal rate) {
        return exchangeRateRepository.save(new ExchangeRate(UUID.randomUUID(), baseCurrency, quoteCurrency, rate, Instant.now()));
    }

    @Transactional(readOnly = true)
    public BigDecimal rateFor(String baseCurrency, String quoteCurrency) {
        return exchangeRateRepository.findLatest(baseCurrency, quoteCurrency)
                .map(ExchangeRate::getRate)
                .or(() -> exchangeRateRepository.findLatest(quoteCurrency, baseCurrency)
                        .map(r -> BigDecimal.ONE.divide(r.getRate(), RATE_SCALE, RoundingMode.HALF_UP)))
                .orElseThrow(() -> new ExchangeRateNotFoundException(baseCurrency, quoteCurrency));
    }

    @Transactional(readOnly = true)
    public Money convert(Money amount, String toCurrency) {
        if (amount.currency().equals(toCurrency)) {
            return amount;
        }
        BigDecimal rate = rateFor(amount.currency(), toCurrency);
        int fraction = Math.max(Currency.getInstance(toCurrency).getDefaultFractionDigits(), 0);
        BigDecimal convertedMajor = amount.toMajor().multiply(rate).setScale(fraction, RoundingMode.HALF_UP);
        long convertedMinor = convertedMajor.movePointRight(fraction).longValueExact();
        return new Money(convertedMinor, toCurrency);
    }

    @Transactional
    public Account clearingAccountFor(String currency) {
        return accountRepository.findByTypeAndCurrency(AccountType.FX_CLEARING, currency)
                .orElseGet(() -> createClearingAccount(currency));
    }

    private Account createClearingAccount(String currency) {
        try {
            return accountService.create("FX Clearing " + currency, AccountType.FX_CLEARING, currency, Long.MAX_VALUE);
        } catch (DataIntegrityViolationException raceLost) {
            return accountRepository.findByTypeAndCurrency(AccountType.FX_CLEARING, currency)
                    .orElseThrow(() -> raceLost);
        }
    }
}