package com.dev.ledger.controller;

import com.dev.ledger.domain.AuditResult;
import com.dev.ledger.dto.BalanceResponse;
import com.dev.ledger.service.AuditService;
import com.dev.ledger.service.BalanceQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/accounts/{id}")
public class QueryController {

    private final BalanceQueryService balanceQueryService;
    private final AuditService auditService;

    public QueryController(BalanceQueryService balanceQueryService, AuditService auditService) {
        this.balanceQueryService = balanceQueryService;
        this.auditService = auditService;
    }

    @GetMapping("/balance")
    public BalanceResponse balanceResponse(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {

        String currency = balanceQueryService.currencyOf(id);
        if (asOf == null) {
            return new BalanceResponse(id, balanceQueryService.currentBalance(id), currency, null);
        }
        return new BalanceResponse(id, balanceQueryService.balanceAsOf(id, asOf), currency, asOf);
    }

    @GetMapping("/audit")
    public AuditResult audit(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return auditService.audit(id, from, to);
    }
}
