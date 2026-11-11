package com.pulsepay.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.pulsepay.transfer.dto.TransferRequest;
import com.pulsepay.transfer.exception.IdempotencyConflictException;
import com.pulsepay.transfer.service.TransferService;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Testcontainers
public class TransferIntegrationTest {
    @Container 
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("pulsepay_test")
            .withUsername("pulsepay")
            .withPassword("pulsepay_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);

    registry.add("spring.datasource.username", POSTGRES::getUsername);

    registry.add("spring.datasource.password", POSTGRES::getPassword);


    }

    @Autowired
    private TransferService transferService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    @BeforeEach 
        void setup(){
            jdbcTemplate.update("DELETE FROM ledger_entries");

            jdbcTemplate.update("DELETE FROM ledger_transactions");
        
            jdbcTemplate.update("DELETE FROM financial_operations");
        
            jdbcTemplate.update("DELETE FROM accounts");
            sourceAccountId=UUID.randomUUID();
            destinationAccountId=UUID.randomUUID();
            createAccount(sourceAccountId, 100000);
            createAccount(destinationAccountId, 20000);
        }

    private void createAccount(UUID accountId, long balance) {
        jdbcTemplate.update("""
                    INSERT INTO accounts (
                    id,
                    owner_id,
                    currency,
                    available_balance,
                    status
                )
                VALUES (?, ?, 'INR', ?, 'ACTIVE')
                """,
                accountId,
                UUID.randomUUID(),
                balance);
    }

    private TransferRequest request(long amount) {
        return new TransferRequest(
                sourceAccountId,
                destinationAccountId,
                amount,
                "INR");
    }

    private long getBalance(UUID accountId) {
        return jdbcTemplate.queryForObject(
                "SELECT available_balance FROM accounts WHERE id = ?",
                Long.class,
                accountId);
    }

    @Test
    void successfulTransferShouldUpdateBothBalancesAndLedger() {
        UUID operationId = UUID.randomUUID();
        var response = transferService.transfer(
                operationId,
                request(50000));
        assertEquals("SUCCEEDED", response.status());
        assertEquals(50000, getBalance(sourceAccountId));
        assertEquals(70000, getBalance(destinationAccountId));
        Integer ledgerCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM ledger_entries
                        WHERE ledger_transaction_id = (
                            SELECT id
                            FROM ledger_transactions
                            WHERE operation_id = ?
                        )
                        """,
                Integer.class,
                operationId);
        assertEquals(2, ledgerCount);
    }

    @Test
    void retryingSameOperationShouldNotTransferMoneyTwice() {
        UUID operationId = UUID.randomUUID();
        var firstResponse = transferService.transfer(
                operationId,
                request(50000));
        var secondResponse = transferService.transfer(
                operationId,
                request(50000));
        assertEquals(firstResponse, secondResponse);
        assertEquals(50000, getBalance(sourceAccountId));
        assertEquals(70000, getBalance(destinationAccountId));
        Integer ledgerCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM ledger_entries le
                        JOIN ledger_transactions lt
                            ON lt.id = le.ledger_transaction_id
                        WHERE lt.operation_id = ?
                        """,
                Integer.class,
                operationId);
        assertEquals(2, ledgerCount);
    }

    @Test
    void insufficientFundsShouldDeclineWithoutChangingBalances() {
        UUID operationId = UUID.randomUUID();
        var response = transferService.transfer(
                operationId,
                request(200000));
        assertEquals("DECLINED", response.status());
        assertEquals("INSUFFICIENT_FUNDS", response.resultCode());
        assertEquals(100000, getBalance(sourceAccountId));
        assertEquals(20000, getBalance(destinationAccountId));
        Integer ledgerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ledger_entries",
                Integer.class);
        assertEquals(0, ledgerCount);
    }

    @Test
    void reusingOperationIdWithDifferentRequestShouldFail() {
        UUID operationId = UUID.randomUUID();
        transferService.transfer(operationId, request(10000));
        assertThrows(
                IdempotencyConflictException.class,
                () -> transferService.transfer(operationId, request(20000)));
        assertEquals(90000, getBalance(sourceAccountId));
        assertEquals(30000, getBalance(destinationAccountId));
    }
}
