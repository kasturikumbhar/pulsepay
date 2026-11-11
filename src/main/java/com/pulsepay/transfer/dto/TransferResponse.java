package com.pulsepay.transfer.dto;

import java.util.UUID;
public record TransferResponse(
    UUID operationId,
    String status,
    String resultCode,
    long amount,
    String currency
) {}
