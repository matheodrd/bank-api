package net.matheodrd.bankapi.dto.response;

import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String accountHolder,
        BigDecimal balance,
        Currency currency,
        AccountStatus status
) {
}
