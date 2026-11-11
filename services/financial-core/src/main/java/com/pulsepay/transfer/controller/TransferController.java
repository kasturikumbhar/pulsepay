package com.pulsepay.transfer.controller;

import com.pulsepay.transfer.dto.TransferRequest;
import com.pulsepay.transfer.dto.TransferResponse;
import com.pulsepay.transfer.service.TransferService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping()
    public ResponseEntity<TransferResponse> transfer(
        @RequestHeader("Idempotency-Key") UUID operationId,
        @Valid @RequestBody TransferRequest request)
    {
        return ResponseEntity.ok(transferService.transfer(operationId, request));
    }
}