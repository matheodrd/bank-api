package net.matheodrd.bankapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.matheodrd.bankapi.model.enums.Currency;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "Account holder is required")
        @Size(min = 2, max = 100)
        String accountHolder,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0")
        BigDecimal initialBalance,

        @NotNull(message = "Currency is required")
        Currency currency
) {
}
