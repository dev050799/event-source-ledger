package com.dev.ledger.controller;

import com.dev.ledger.dto.ExchangeRateDto.RateResponse;
import com.dev.ledger.dto.ExchangeRateDto.SetRateRequest;
import com.dev.ledger.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    public ResponseEntity<RateResponse> setRate(@Valid @RequestBody SetRateRequest req) {
        var rate = exchangeRateService.setRate(req.baseCurrency(), req.quoteCurrency(), req.rate());
        return ResponseEntity.status(HttpStatus.CREATED).body(RateResponse.from(rate));
    }

    @GetMapping("/{base}/{quote}")
    public RateResponse getRate(@PathVariable String base, @PathVariable String quote) {
        var rate = exchangeRateService.rateFor(base, quote);
        return new RateResponse(base, quote, rate, null);
    }
}