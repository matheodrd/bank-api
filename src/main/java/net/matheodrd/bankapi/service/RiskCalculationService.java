package net.matheodrd.bankapi.service;

import lombok.extern.slf4j.Slf4j;
import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RiskCalculationService {

    private final TransactionRepository transactionRepository;

    public RiskCalculationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public int calculateRiskScore(UUID accountId, BigDecimal amount, LocalDateTime timestamp) {
        int score = 0;

        // Amount > 10,000 -> +30 points
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            score += 30;
            log.debug("Risk +30: High amount {}", amount);
        }

        // Transaction between 23h and 6h -> +20 points
        int hour = timestamp.getHour();
        if (hour == 23 || hour < 6) {
            score += 20;
            log.debug("Risk +20: Night transaction at {}h", hour);
        }

        // More than 5 transactions in 1 hour -> +40 points
        LocalDateTime oneHourAgo = timestamp.minusHours(1);
        List<Transaction> recentTransactions = transactionRepository
                .findRecentByAccountId(accountId, oneHourAgo);

        if (recentTransactions.size() >= 5) {
            score += 40;
            log.warn("Risk +40: {} transactions in last hour for account {}",
                    recentTransactions.size(), accountId);
        }

        return Math.min(score, 100);
    }

    public TransactionStatus determineStatus(int riskScore) {
        return riskScore > 70 ? TransactionStatus.FLAGGED : TransactionStatus.COMPLETED;
    }
}
