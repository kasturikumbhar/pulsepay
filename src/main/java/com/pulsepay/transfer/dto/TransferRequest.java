package com.pulsepay.transfer.dto;

import java.util.UUID;
import jakarta.validation.constraints.*;

public record TransferRequest(
    @NotNull
    UUID sourceAccountId,
    @NotNull
    UUID destinationAccountId,
    @Positive
    long amount,
    @NotNull 
    @Pattern(regexp="^[A-Z]{3}$")
    String currency

){

}
