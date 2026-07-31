package com.dev.ledger.controller;

import com.dev.ledger.dto.AccountDto.AccountResponse;
import com.dev.ledger.dto.AccountDto.CreateAccountRequest;
import com.dev.ledger.entity.Account;
import com.dev.ledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        Account a = accountService.create(req.name(), req.type(), req.currency(), req.creditLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(a));
    }

    @GetMapping("{id}")
    public AccountResponse get(@PathVariable UUID id){
        return AccountResponse.from(accountService.get(id));
    }
}
