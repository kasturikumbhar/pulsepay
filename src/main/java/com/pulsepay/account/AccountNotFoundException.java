package com.pulsepay.account;
import java.util.UUID;
public class AccountNotFoundException extends RuntimeException{

    public AccountNotFoundException(UUID accountId){
        super("Account not Found" + accountId);
    }


}
