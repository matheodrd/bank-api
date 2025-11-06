package net.matheodrd.bankapi.dto.request;

import jakarta.validation.constraints.NotNull;
import net.matheodrd.bankapi.model.enums.AccountStatus;

public record UpdateAccountStatusRequest(
        @NotNull
        AccountStatus status
) {
}
