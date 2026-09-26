package com.dev.ledger.controller;

import com.dev.ledger.domain.Money;
import com.dev.ledger.dto.TransactionDto.TransactionResponse;
import com.dev.ledger.dto.TransferDto.TransferRequest;
import com.dev.ledger.service.LedgerService;
import com.dev.ledger.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;
    private final LedgerService ledgerService;

    public TransferController(TransferService transferService, LedgerService ledgerService) {
        this.transferService = transferService;
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest req) {

        UUID id = transferService.transfer(req.fromAccountId(), req.toAccountId(),
                Money.of(req.amount(), req.currency()), idempotencyKey, req.type(), req.metadata());
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.transactionResponse(id));
    }
}