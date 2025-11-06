package net.matheodrd.bankapi.dto.response;

import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountDetailResponse(
        UUID id,
        String accountNumber,
        String accountHolder,
        BigDecimal balance,
        Currency currency,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long totalTransactions,  // Calculated stats
        BigDecimal totalDebits,
        BigDecimal totalCredits
) {
}
