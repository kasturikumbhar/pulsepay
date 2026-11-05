package com.pulsepay.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
public record CreateAccountRequest(
    @NotNull(message="Owner ID is required")
    UUID ownerId,

    @NotNull(message="Currency is required")
    @Pattern(
        regexp="^[A-Z]{3}$",
        message="Currency must be a 3-letter uppercase code"
    )
    String currency
){    
}
