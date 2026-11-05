package com.pulsepay.api;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.pulsepay.account.AccountNotFoundException;



import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler{
    public ResponseEntity<Map<String, String>> handleAccountNotFoundException(AccountNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

} 
