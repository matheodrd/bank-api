package net.matheodrd.bankapi.mapper;

import net.matheodrd.bankapi.dto.request.CreateAccountRequest;
import net.matheodrd.bankapi.dto.response.AccountResponse;
import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AccountMapper {

    AccountResponse toResponse(Account account);

    List<AccountResponse> toResponseList(List<Account> accounts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toEntity(CreateAccountRequest request);

    default Account toEntityWithDefaults(CreateAccountRequest request, String accountNumber) {
        Account account = toEntity(request);
        account.setAccountNumber(accountNumber);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }
}
