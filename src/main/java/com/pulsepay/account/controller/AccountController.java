package com.pulsepay.account.controller;

import com.pulsepay.account.service.AccountService;
import com.pulsepay.account.dto.AccountResponse;
import com.pulsepay.account.dto.CreateAccountRequest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService= accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
        @Valid 
        @RequestBody
         CreateAccountRequest request
    ){
        AccountResponse response= accountService.createAccount(
            request.ownerId(), 
            request.currency()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(
        @PathVariable UUID accountId
    ){
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
    
}






