package net.matheodrd.bankapi.service;

import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskCalculationService Tests")
class RiskCalculationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RiskCalculationService riskCalculationService;

    private UUID accountId;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        timestamp = LocalDateTime.of(2025, 1, 15, 14, 30);
    }

    @Test
    @DisplayName("Should return 0 risk score for normal transaction")
    void shouldReturnZeroRiskScoreForNormalTransaction() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        when(transactionRepository.findRecentByAccountId(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // When
        int riskScore = riskCalculationService.calculateRiskScore(accountId, amount, timestamp);

        // Then
        assertThat(riskScore).isEqualTo(0);
    }

    @Test
    @DisplayName("Should add 30 points for high amount (> 10,000)")
    void shouldAdd30PointsForHighAmount() {
        // Given
        BigDecimal amount = new BigDecimal("15000.00");
        when(transactionRepository.findRecentByAccountId(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // When
        int riskScore = riskCalculationService.calculateRiskScore(accountId, amount, timestamp);

        // Then
        assertThat(riskScore).isEqualTo(30);
    }

    @Test
    @DisplayName("Should add 20 points for night transaction (23h-6h)")
    void shouldAdd20PointsForNightTransaction() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        LocalDateTime nightTime = LocalDateTime.of(2025, 1, 15, 2, 30);
        when(transactionRepository.findRecentByAccountId(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // When
        int riskScore = riskCalculationService.calculateRiskScore(accountId, amount, nightTime);

        // Then
        assertThat(riskScore).isEqualTo(20);
    }

    @Test
    @DisplayName("Should add 40 points for more than 5 transactions in last hour")
    void shouldAdd40PointsForFrequentTransactions() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        List<Transaction> recentTransactions = createMockTransactions(6);
        when(transactionRepository.findRecentByAccountId(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(recentTransactions);

        // When
        int riskScore = riskCalculationService.calculateRiskScore(accountId, amount, timestamp);

        // Then
        assertThat(riskScore).isEqualTo(40);
    }

    @Test
    @DisplayName("Should calculate cumulative risk score")
    void shouldCalculateCumulativeRiskScore() {
        // Given: High amount + night + frequent transactions
        BigDecimal amount = new BigDecimal("15000.00");
        LocalDateTime nightTime = LocalDateTime.of(2025, 1, 15, 23, 30);
        List<Transaction> recentTransactions = createMockTransactions(6);
        when(transactionRepository.findRecentByAccountId(eq(accountId), any(LocalDateTime.class)))
                .thenReturn(recentTransactions);

        // When
        int riskScore = riskCalculationService.calculateRiskScore(accountId, amount, nightTime);

        // Then
        assertThat(riskScore).isEqualTo(90); // 30 + 20 + 40
    }

    @Test
    @DisplayName("Should determine COMPLETED status for low risk (score <= 70)")
    void shouldDetermineCompletedStatusForLowRisk() {
        // When
        TransactionStatus status = riskCalculationService.determineStatus(50);

        // Then
        assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should determine FLAGGED status for high risk (score > 70)")
    void shouldDetermineFlaggedStatusForHighRisk() {
        // When
        TransactionStatus status = riskCalculationService.determineStatus(75);

        // Then
        assertThat(status).isEqualTo(TransactionStatus.FLAGGED);
    }

    @Test
    @DisplayName("Should determine COMPLETED status for risk score exactly 70")
    void shouldDetermineCompletedStatusForRiskScore70() {
        // When
        TransactionStatus status = riskCalculationService.determineStatus(70);

        // Then
        assertThat(status).isEqualTo(TransactionStatus.COMPLETED);
    }

    // Helper method
    private List<Transaction> createMockTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Transaction transaction = new Transaction();
            transaction.setId(UUID.randomUUID());
            transaction.setAccountId(accountId);
            transactions.add(transaction);
        }
        return transactions;
    }
}
