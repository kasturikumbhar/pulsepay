package com.pulsepay.transfer.dto;

import java.util.UUID;
public record Operation(
    UUID operationId,
    String requestHash,
    String status,
    String resultCode,
    long amount,
    String currency
){}