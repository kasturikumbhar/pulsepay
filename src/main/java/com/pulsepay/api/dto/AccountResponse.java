package com.pulsepay.api.dto;

import java.util.UUID;
import java.time.Instant;
public record AccountResponse(
    UUID id,
    UUID ownerId,
    String currency,
    long availableBalance,
    String status,
    Instant createdAt
){}
