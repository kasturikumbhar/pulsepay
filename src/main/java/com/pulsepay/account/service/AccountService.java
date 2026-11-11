package com.pulsepay.account.service;

import com.pulsepay.account.repository.AccountRepository;
import com.pulsepay.account.AccountNotFoundException;
import com.pulsepay.account.dto.AccountResponse;
import com.pulsepay.account.entity.Account;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;


@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository =accountRepository;
    }
    
    @Transactional 
    public AccountResponse createAccount( UUID ownerId, String currency){

        Account account = accountRepository.create(ownerId, currency);
        return new AccountResponse(
            account.id(),
            account.ownerId(),
            account.currency(),
            account.availableBalance(),
            account.status(),
            account.createdAt());

    }

    @Transactional(readOnly=true)
    public AccountResponse getAccount(UUID accountId){

        Account account= accountRepository.findById(accountId)
            .orElseThrow(()-> new AccountNotFoundException(accountId));
        
        return new AccountResponse(
            account.id(),
            account.ownerId(),
            account.currency(),
            account.availableBalance(),
            account.status(),
            account.createdAt()
        );

    }
}
