package com.dev.ledger.controller;

import com.dev.ledger.domain.Money;
import com.dev.ledger.domain.PostTransactionCommand;
import com.dev.ledger.domain.PostTransactionCommand.Leg;
import com.dev.ledger.domain.Side;
import com.dev.ledger.dto.TransactionDto.ReverseRequest;
import com.dev.ledger.dto.TransactionDto.PostTransactionRequest;
import com.dev.ledger.dto.TransactionDto.TransactionResponse;
import com.dev.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> post(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PostTransactionRequest req) {

        List<Leg> legs = req.legs().stream()
                .map(l -> new Leg(
                        l.accountId(), Side.fromCode(l.direction()), Money.of(l.amount(), l.currency()))).toList();

        PostTransactionCommand command = new PostTransactionCommand(idempotencyKey, req.type(),
                req.effectiveAt() != null ? req.effectiveAt() : Instant.now(),
                legs, req.metadata(), null);

        UUID id = ledgerService.post(command);
        return ResponseEntity.status((HttpStatus.CREATED)).body(ledgerService.transactionResponse(id));
    }

    @PostMapping("/{id}/reversal")
    public ResponseEntity<TransactionResponse> reverse(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) ReverseRequest req) {
        UUID reversalId = ledgerService.reverse(id, idempotencyKey, req == null ? null : req.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.transactionResponse(reversalId));
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable UUID id) {
        return ledgerService.transactionResponse(id);
    }


}
