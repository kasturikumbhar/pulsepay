package com.pulsepay.transfer.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepay.transfer.dto.LockedAccount;
import com.pulsepay.transfer.dto.Operation;
import com.pulsepay.transfer.dto.TransferRequest;
import com.pulsepay.transfer.dto.TransferResponse;
import com.pulsepay.transfer.exception.IdempotencyConflictException;
import com.pulsepay.transfer.repository.TransferRepository;

@Service 
public class TransferService {
    private final TransferRepository repository;

    public TransferService(TransferRepository repository) {
        this.repository = repository;
    }

    @Transactional 
    public TransferResponse transfer(
            UUID operationId,
            TransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("Source and Destination Accounts cannot be same!");
        }
        String requestHash = calculateHash(request);
        boolean newOperation = repository.reserveOperation(operationId, requestHash, request.sourceAccountId(), request.destinationAccountId(),
                request.amount(), request.currency());
        if (!newOperation) {
            Operation existing = repository.findOperation(operationId);
            if (!existing.requestHash().equals(requestHash)) {
                throw new IdempotencyConflictException();
            }
            return toResponse(existing);
        }
        // lock both accounts for transfer
        List<LockedAccount> accounts = repository.lockAccounts(request.sourceAccountId(),
                request.destinationAccountId());
        Map<UUID, LockedAccount> accountMap = accounts.stream()
                .collect(Collectors.toMap(LockedAccount::id, Function.identity()));
        var source = accountMap.get(request.sourceAccountId());
        var destination = accountMap.get(request.destinationAccountId());
        if (source == null || destination == null) {
            return decline(operationId, request, "ACCOUNT_NOT_FOUND");
        }
        if (!source.status().equals("ACTIVE")
                || !destination.status().equals("ACTIVE")) {
            return decline(operationId, request, "ACCOUNT_NOT_ACTIVE");
        }
        if (!source.currency().equals(request.currency())
                || !destination.currency().equals(request.currency())) {
            return decline(operationId, request, "CURRENCY_MISMATCH");
        }
        if (source.balance() < request.amount()) {
            return decline(operationId, request, "INSUFFICIENT_FUNDS");
        }
        long newSourceBalance = source.balance() - request.amount();
        long newDestinationBalance = Math.addExact(destination.balance(), request.amount());
        // Update both accounts.
        repository.updateBalance(
                source.id(),
                newSourceBalance);
        repository.updateBalance(
                destination.id(),
                newDestinationBalance);
        // Create one ledger transaction.
        UUID ledgerTransactionId = repository.createLedgerTransaction(operationId);
        // Debit source account.
        repository.insertLedgerEntry(
                ledgerTransactionId,
                source.id(),
                "DEBIT",
                request.amount(),
                request.currency());
        // Credit destination account.
        repository.insertLedgerEntry(
                ledgerTransactionId,
                destination.id(),
                "CREDIT",
                request.amount(),
                request.currency());
        // Mark financial operation successful.
        repository.completeOperation(
                operationId,
                "SUCCEEDED",
                "TRANSFER_COMPLETED");
        return new TransferResponse(
                operationId,
                "SUCCEEDED",
                "TRANSFER_COMPLETED",
                request.amount(),
                request.currency());
    }

    private TransferResponse decline(
            UUID operationId,
            TransferRequest request,
            String resultCode) {
        repository.completeOperation(
                operationId,
                "DECLINED",
                resultCode);
        return new TransferResponse(
                operationId,
                "DECLINED",
                resultCode,
                request.amount(),
                request.currency());
    }

    private TransferResponse toResponse(
            Operation operation) {
        return new TransferResponse(
                operation.operationId(),
                operation.status(),
                operation.resultCode(),
                operation.amount(),
                operation.currency());
    }

    private String calculateHash(TransferRequest request) {
        String canonicalRequest = request.sourceAccountId() + "|" +
                request.destinationAccountId() + "|" +
                request.amount() + "|" +
                request.currency();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
