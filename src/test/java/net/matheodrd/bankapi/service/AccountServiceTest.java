package net.matheodrd.bankapi.service;

import net.matheodrd.bankapi.dto.request.CreateAccountRequest;
import net.matheodrd.bankapi.dto.request.UpdateAccountStatusRequest;
import net.matheodrd.bankapi.dto.response.AccountDetailResponse;
import net.matheodrd.bankapi.dto.response.AccountResponse;
import net.matheodrd.bankapi.exception.EntityNotFoundException;
import net.matheodrd.bankapi.mapper.AccountMapper;
import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.repository.AccountRepository;
import net.matheodrd.bankapi.repository.projection.AccountDetailProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountResponse accountResponse;
    private CreateAccountRequest createRequest;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("GB29NWBK60161331926819")
                .accountHolder("John Doe")
                .balance(new BigDecimal("1000.00"))
                .currency(Currency.GBP)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accountResponse = new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountHolder(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus()
        );

        createRequest = new CreateAccountRequest(
                "John Doe",
                new BigDecimal("1000.00"),
                Currency.GBP
        );
    }

    @Test
    @DisplayName("Should find all accounts with pagination")
    void shouldFindAllAccountsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> accountPage = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(pageable)).thenReturn(accountPage);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        Page<AccountResponse> result = accountService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(accountResponse);
        verify(accountRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should find account by id with details")
    void shouldFindAccountByIdWithDetails() {
        // Given
        UUID accountId = account.getId();
        AccountDetailProjection projection = mock(AccountDetailProjection.class);
        AccountDetailResponse detailResponse = new AccountDetailResponse(
                accountId,
                "GB29NWBK60161331926819",
                "John Doe",
                new BigDecimal("1000.00"),
                Currency.GBP,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5L,
                new BigDecimal("200.00"),
                new BigDecimal("300.00")
        );

        when(accountRepository.findDetailById(accountId)).thenReturn(Optional.of(projection));
        when(accountMapper.toDetailResponse(projection)).thenReturn(detailResponse);

        // When
        AccountDetailResponse result = accountService.findById(accountId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(accountId);
        verify(accountRepository).findDetailById(accountId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findDetailById(accountId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> accountService.findById(accountId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Account not found");

        verify(accountRepository).findDetailById(accountId);
    }

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccountSuccessfully() {
        // Given
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountMapper.toEntityWithDefaults(any(CreateAccountRequest.class), anyString()))
                .thenReturn(account);
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        AccountResponse result = accountService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accountHolder()).isEqualTo("John Doe");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Should update account status successfully")
    void shouldUpdateAccountStatusSuccessfully() {
        // Given
        UUID accountId = account.getId();
        UpdateAccountStatusRequest statusRequest = new UpdateAccountStatusRequest(AccountStatus.SUSPENDED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        // When
        AccountResponse result = accountService.updateStatus(accountId, statusRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        verify(accountRepository).findById(accountId);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent account")
    void shouldThrowExceptionWhenUpdatingNonExistentAccount() {
        // Given
        UUID accountId = UUID.randomUUID();
        UpdateAccountStatusRequest statusRequest = new UpdateAccountStatusRequest(AccountStatus.SUSPENDED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> accountService.updateStatus(accountId, statusRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Account not found");
    }
}
