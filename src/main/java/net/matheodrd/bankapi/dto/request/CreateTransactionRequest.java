package net.matheodrd.bankapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.matheodrd.bankapi.model.enums.TransactionCategory;
import net.matheodrd.bankapi.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        @NotNull
        TransactionType type,

        @NotNull
        TransactionCategory category,

        @Size(max = 500)
        String description
) {
}
