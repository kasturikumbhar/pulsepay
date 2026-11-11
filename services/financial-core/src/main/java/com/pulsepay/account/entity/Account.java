package com.pulsepay.account.entity;
import java.util.UUID;
import java.time.Instant;
public record Account(
    UUID id,
    UUID ownerId,
    String currency,
    long availableBalance,
    String status,
    Instant createdAt
){}
