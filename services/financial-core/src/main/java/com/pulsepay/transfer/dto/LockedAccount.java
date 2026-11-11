package com.pulsepay.transfer.dto;

import java.util.UUID;

public record LockedAccount(
    UUID id, 
    long balance,
    String currency,
    String status){}
