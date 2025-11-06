package net.matheodrd.bankapi.dto.response;

import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.model.enums.TransactionCategory;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        Currency currency,
        TransactionType type,
        TransactionCategory category,
        String description,
        TransactionStatus status,
        Integer riskScore,
        LocalDateTime timestamp
) {
}
