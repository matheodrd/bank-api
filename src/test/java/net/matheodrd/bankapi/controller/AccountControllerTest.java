package net.matheodrd.bankapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.matheodrd.bankapi.dto.request.CreateAccountRequest;
import net.matheodrd.bankapi.dto.request.UpdateAccountStatusRequest;
import net.matheodrd.bankapi.dto.response.AccountDetailResponse;
import net.matheodrd.bankapi.dto.response.AccountResponse;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.model.enums.TransactionCategory;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;
import net.matheodrd.bankapi.service.AccountService;
import net.matheodrd.bankapi.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private TransactionService transactionService;

    private AccountResponse accountResponse;
    private AccountDetailResponse accountDetailResponse;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();

        accountResponse = new AccountResponse(
                accountId,
                "GB29NWBK60161331926819",
                "John Doe",
                new BigDecimal("1000.00"),
                Currency.GBP,
                AccountStatus.ACTIVE
        );

        accountDetailResponse = new AccountDetailResponse(
                accountId,
                "GB29NWBK60161331926819",
                "John Doe",
                new BigDecimal("1000.00"),
                Currency.GBP,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                5L,
                new BigDecimal("500.00"),
                new BigDecimal("0.00")
        );
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Should return paginated accounts")
    void shouldReturnPaginatedAccounts() throws Exception {
        // Given
        Page<AccountResponse> accountPage = new PageImpl<>(
                List.of(accountResponse),
                PageRequest.of(0, 20),
                1
        );
        when(accountService.findAll(any(Pageable.class))).thenReturn(accountPage);

        // When/Then
        mockMvc.perform(get("/api/v1/accounts")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(accountId.toString()))
                .andExpect(jsonPath("$.content[0].accountHolder").value("John Doe"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.00))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Should use default pagination parameters")
    void shouldUseDefaultPaginationParameters() throws Exception {
        // Given
        Page<AccountResponse> accountPage = new PageImpl<>(List.of());
        when(accountService.findAll(any(Pageable.class))).thenReturn(accountPage);

        // When/Then
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Should return account by ID")
    void shouldReturnAccountById() throws Exception {
        // Given
        when(accountService.findById(accountId)).thenReturn(accountDetailResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.accountHolder").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.totalTransactions").value(5))
                .andExpect(jsonPath("$.totalDebits").value(500.00))
                .andExpect(jsonPath("$.totalCredits").value(0.00));
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Should create new account")
    void shouldCreateNewAccount() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest(
                "Jane Doe",
                new BigDecimal("2000.00"),
                Currency.EUR
        );

        AccountResponse newAccount = new AccountResponse(
                UUID.randomUUID(),
                "FR1420041010050500013M02606",
                "Jane Doe",
                new BigDecimal("2000.00"),
                Currency.EUR,
                AccountStatus.ACTIVE
        );

        when(accountService.create(any(CreateAccountRequest.class))).thenReturn(newAccount);

        // When/Then
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.accountHolder").value("Jane Doe"))
                .andExpect(jsonPath("$.balance").value(2000.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Should return 400 for invalid request")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        // Given - Invalid request with null accountHolder
        String invalidRequest = """
                {
                    "accountHolder": null,
                    "initialBalance": 1000.00,
                    "currency": "GBP"
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/accounts/{id}/status - Should update account status")
    void shouldUpdateAccountStatus() throws Exception {
        // Given
        UpdateAccountStatusRequest request = new UpdateAccountStatusRequest(AccountStatus.SUSPENDED);

        AccountResponse updatedAccount = new AccountResponse(
                accountId,
                accountResponse.accountNumber(),
                accountResponse.accountHolder(),
                accountResponse.balance(),
                accountResponse.currency(),
                AccountStatus.SUSPENDED
        );

        when(accountService.updateStatus(eq(accountId), any(UpdateAccountStatusRequest.class)))
                .thenReturn(updatedAccount);

        // When/Then
        mockMvc.perform(patch("/api/v1/accounts/{id}/status", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/transactions - Should return account transactions")
    void shouldReturnAccountTransactions() throws Exception {
        // Given
        TransactionResponse transaction = new TransactionResponse(
                UUID.randomUUID(),
                accountId,
                new BigDecimal("100.00"),
                Currency.GBP,
                TransactionType.DEBIT,
                TransactionCategory.PAYMENT,
                "Test payment",
                TransactionStatus.COMPLETED,
                0,
                LocalDateTime.now()
        );

        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(0, 20),
                1
        );

        when(transactionService.findByAccountId(eq(accountId), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", accountId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.content[0].amount").value(100.00))
                .andExpect(jsonPath("$.content[0].type").value("DEBIT"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/transactions - Should use default pagination")
    void shouldUseDefaultPaginationForTransactions() throws Exception {
        // Given
        Page<TransactionResponse> emptyPage = new PageImpl<>(List.of());
        when(transactionService.findByAccountId(eq(accountId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When/Then
        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
