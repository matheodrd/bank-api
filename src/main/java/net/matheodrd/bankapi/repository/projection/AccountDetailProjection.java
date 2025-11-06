package net.matheodrd.bankapi.repository.projection;

import net.matheodrd.bankapi.dto.response.AccountDetailResponse;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AccountDetailProjection {
    UUID getId();

    String getAccountNumber();

    String getAccountHolder();

    BigDecimal getBalance();

    Currency getCurrency();

    AccountStatus getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    Long getTotalTransactions();

    BigDecimal getTotalDebits();

    BigDecimal getTotalCredits();

    default AccountDetailResponse toResponse() {
        return new AccountDetailResponse(
                getId(), getAccountNumber(), getAccountHolder(), getBalance(),
                getCurrency(), getStatus(), getCreatedAt(), getUpdatedAt(),
                getTotalTransactions(), getTotalDebits(), getTotalCredits()
        );
    }
}