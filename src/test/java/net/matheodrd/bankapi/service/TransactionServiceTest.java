package net.matheodrd.bankapi.service;

import net.matheodrd.bankapi.dto.request.CreateTransactionRequest;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.exception.AccountSuspendedException;
import net.matheodrd.bankapi.exception.EntityNotFoundException;
import net.matheodrd.bankapi.exception.InsufficientBalanceException;
import net.matheodrd.bankapi.mapper.TransactionMapper;
import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.*;
import net.matheodrd.bankapi.repository.AccountRepository;
import net.matheodrd.bankapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RiskCalculationService riskCalculationService;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;
    private CreateTransactionRequest transactionRequest;
    private Transaction transaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("GB29NWBK60161331926819")
                .accountHolder("John Doe")
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.GBP)
                .status(AccountStatus.ACTIVE)
                .build();

        transactionRequest = new CreateTransactionRequest(
                account.getId(),
                new BigDecimal("100.00"),
                TransactionType.DEBIT,
                TransactionCategory.PAYMENT,
                "Test payment"
        );

        transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(account.getId())
                .amount(new BigDecimal("100.00"))
                .currency(Currency.GBP)
                .type(TransactionType.DEBIT)
                .category(TransactionCategory.PAYMENT)
                .description("Test payment")
                .status(TransactionStatus.COMPLETED)
                .riskScore(0)
                .timestamp(LocalDateTime.now())
                .build();

        transactionResponse = new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getRiskScore(),
                transaction.getTimestamp()
        );
    }

    @Test
    @DisplayName("Should create transaction successfully for COMPLETED status")
    void shouldCreateTransactionSuccessfully() {
        // Given
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(riskCalculationService.calculateRiskScore(any(), any(), any())).thenReturn(50);
        when(riskCalculationService.determineStatus(50)).thenReturn(TransactionStatus.COMPLETED);
        when(transactionMapper.toEntityWithCalculatedFields(any(), any(), any(), anyInt(), any()))
                .thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.create(transactionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("900.00")); // 1000 - 100
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should create FLAGGED transaction and not update balance")
    void shouldCreateFlaggedTransactionWithoutBalanceUpdate() {
        // Given
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(riskCalculationService.calculateRiskScore(any(), any(), any())).thenReturn(80);
        when(riskCalculationService.determineStatus(80)).thenReturn(TransactionStatus.FLAGGED);

        Transaction flaggedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(account.getId())
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.FLAGGED)
                .riskScore(80)
                .build();

        TransactionResponse flaggedResponse = new TransactionResponse(
                flaggedTransaction.getId(),
                flaggedTransaction.getAccountId(),
                flaggedTransaction.getAmount(),
                Currency.GBP,
                TransactionType.DEBIT,
                TransactionCategory.PAYMENT,
                "Test",
                TransactionStatus.FLAGGED,
                80,
                LocalDateTime.now()
        );

        when(transactionMapper.toEntityWithCalculatedFields(any(), any(), any(), anyInt(), any()))
                .thenReturn(flaggedTransaction);
        when(transactionRepository.save(flaggedTransaction)).thenReturn(flaggedTransaction);
        when(transactionMapper.toResponse(flaggedTransaction)).thenReturn(flaggedResponse);

        BigDecimal originalBalance = account.getBalance();

        // When
        TransactionResponse result = transactionService.create(transactionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TransactionStatus.FLAGGED);
        assertThat(account.getBalance()).isEqualTo(originalBalance); // Balance unchanged
        verify(transactionRepository).save(flaggedTransaction);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        when(accountRepository.findById(account.getId())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> transactionService.create(transactionRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Account not found");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccountSuspendedException when account is suspended")
    void shouldThrowExceptionWhenAccountSuspended() {
        // Given
        account.setStatus(AccountStatus.SUSPENDED);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        // When/Then
        assertThatThrownBy(() -> transactionService.create(transactionRequest))
                .isInstanceOf(AccountSuspendedException.class)
                .hasMessageContaining("Account is suspended");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException for DEBIT when balance insufficient")
    void shouldThrowExceptionWhenInsufficientBalance() {
        // Given
        CreateTransactionRequest largeDebitRequest = new CreateTransactionRequest(
                account.getId(),
                new BigDecimal("2000.00"), // More than balance
                TransactionType.DEBIT,
                TransactionCategory.WITHDRAWAL,
                "Large withdrawal"
        );

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        // When/Then
        assertThatThrownBy(() -> transactionService.create(largeDebitRequest))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create CREDIT transaction and increase balance")
    void shouldCreateCreditTransactionAndIncreaseBalance() {
        // Given
        CreateTransactionRequest creditRequest = new CreateTransactionRequest(
                account.getId(),
                new BigDecimal("500.00"),
                TransactionType.CREDIT,
                TransactionCategory.DEPOSIT,
                "Deposit"
        );

        Transaction creditTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .accountId(account.getId())
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.COMPLETED)
                .riskScore(0)
                .build();

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(riskCalculationService.calculateRiskScore(any(), any(), any())).thenReturn(0);
        when(riskCalculationService.determineStatus(0)).thenReturn(TransactionStatus.COMPLETED);
        when(transactionMapper.toEntityWithCalculatedFields(any(), any(), any(), anyInt(), any()))
                .thenReturn(creditTransaction);
        when(transactionRepository.save(creditTransaction)).thenReturn(creditTransaction);
        when(transactionMapper.toResponse(creditTransaction)).thenReturn(transactionResponse);

        // When
        transactionService.create(creditRequest);

        // Then
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1500.00")); // 1000 + 500
    }

    @Test
    @DisplayName("Should find transaction by id")
    void shouldFindTransactionById() {
        // Given
        UUID transactionId = transaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        // When
        TransactionResponse result = transactionService.findById(transactionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(transactionId);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> transactionService.findById(transactionId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }
}
