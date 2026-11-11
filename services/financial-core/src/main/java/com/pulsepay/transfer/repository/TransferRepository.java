package com.pulsepay.transfer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.pulsepay.transfer.dto.LockedAccount;
import com.pulsepay.transfer.dto.Operation;

@Repository
public class TransferRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransferRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean reserveOperation(
            UUID operationId,
            String requestHash,
            UUID sourceId,
            UUID destinationId,
            long amount,
            String currency) {

        String sql = """
                INSERT INTO financial_operations(
                operation_id,
                request_hash,
                source_account_id,
                destination_account_id,
                amount,
                currency,
                status)
                VALUES(?,?,?,?,?,?,'PROCESSING')
                ON CONFLICT (operation_id) DO NOTHING
                """;

        return jdbcTemplate.update(sql, operationId, requestHash, sourceId, destinationId, amount, currency) == 1;
    }

    public Operation findOperation(UUID operationId) {
        String sql = """
                SELECT operation_id, request_hash,
                    status, result_code, amount, currency
                FROM financial_operations
                WHERE operation_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Operation(
                rs.getObject("operation_id", UUID.class),
                rs.getString("request_hash"),
                rs.getString("status"),
                rs.getString("result_code"),
                rs.getLong("amount"),
                rs.getString("currency")), operationId);

    }

    public List<LockedAccount> lockAccounts(
            UUID sourceId,
            UUID destinationId) {
        String sql = """
                SELECT id, available_balance, currency, status
                FROM accounts WHERE id IN (?, ?)
                ORDER BY id
                FOR UPDATE
                """;

        return jdbcTemplate.query(sql, (rs, rowNums) -> new LockedAccount(
                rs.getObject("id", UUID.class),
                rs.getLong("available_balance"),
                rs.getString("currency").trim(),
                rs.getString("status")

        ), sourceId, destinationId);
    }

    public void updateBalance(UUID accountId, long newBalance) {
        String sql = """
                Update accounts
                SET available_balance= ?,
                updated_at=NOW()
                where id=?
                """;

        jdbcTemplate.update(sql, newBalance, accountId);

    }

    public UUID createLedgerTransaction(UUID operationId) {
        String sql = """
                INSERT INTO ledger_transactions(operation_id)
                VALUES(?)
                RETURNING id
                """;
        return jdbcTemplate.queryForObject(sql, UUID.class, operationId);

    }

    public void insertLedgerEntry(
            UUID transactionId,
            UUID accountId,
            String entryType,
            long amount,
            String currency

    ) {
        String sql = """
                INSERT INTO ledger_entries (
                ledger_transaction_id, account_id, entry_type, amount, currency )
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql, transactionId, accountId, entryType, amount, currency );

    }

    public void completeOperation(
            UUID operationId,
            String status,
            String resultCode) {
        String sql = """

                UPDATE financial_operations
                SET status = ?,
                    result_code = ?,
                    completed_at = NOW()
                WHERE operation_id = ?
                """;
        jdbcTemplate.update(sql, status, resultCode, operationId);

    }
}
