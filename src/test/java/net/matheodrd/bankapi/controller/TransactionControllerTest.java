package net.matheodrd.bankapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.matheodrd.bankapi.dto.request.CreateTransactionRequest;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.model.enums.TransactionCategory;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private UUID accountId;
    private UUID transactionId;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        transactionResponse = new TransactionResponse(
                transactionId,
                accountId,
                new BigDecimal("250.00"),
                Currency.GBP,
                TransactionType.DEBIT,
                TransactionCategory.PAYMENT,
                "Online purchase",
                TransactionStatus.COMPLETED,
                15,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should return all transactions")
    void shouldReturnAllTransactions() throws Exception {
        // Given
        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transactionResponse),
                PageRequest.of(0, 20),
                1
        );
        when(transactionService.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()))
                .andExpect(jsonPath("$.content[0].amount").value(250.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should filter by account ID")
    void shouldFilterTransactionsByAccountId() throws Exception {
        // Given
        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transactionResponse),
                PageRequest.of(0, 20),
                1
        );
        when(transactionService.findByFilters(eq(accountId), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("accountId", accountId.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].accountId").value(accountId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should filter by status")
    void shouldFilterTransactionsByStatus() throws Exception {
        // Given
        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transactionResponse),
                PageRequest.of(0, 20),
                1
        );
        when(transactionService.findByFilters(any(), eq(TransactionStatus.COMPLETED), any(), any(), any(), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "COMPLETED")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should filter by type")
    void shouldFilterTransactionsByType() throws Exception {
        // Given
        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transactionResponse),
                PageRequest.of(0, 20),
                1
        );
        when(transactionService.findByFilters(any(), any(), eq(TransactionType.DEBIT), any(), any(), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("type", "DEBIT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].type").value("DEBIT"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should filter by date range")
    void shouldFilterTransactionsByDateRange() throws Exception {
        // Given
        Page<TransactionResponse> transactionPage = new PageImpl<>(
                List.of(transactionResponse),
                PageRequest.of(0, 20),
                1
        );
        when(transactionService.findByFilters(any(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(transactionPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("fromDate", "2025-01-01T00:00:00")
                        .param("toDate", "2025-12-31T23:59:59")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Should use default pagination")
    void shouldUseDefaultPagination() throws Exception {
        // Given
        Page<TransactionResponse> emptyPage = new PageImpl<>(List.of());
        when(transactionService.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - Should return transaction by ID")
    void shouldReturnTransactionById() throws Exception {
        // Given
        when(transactionService.findById(transactionId)).thenReturn(transactionResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.riskScore").value(15));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - Should create new transaction")
    void shouldCreateNewTransaction() throws Exception {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(
                accountId,
                new BigDecimal("150.00"),
                TransactionType.CREDIT,
                TransactionCategory.TRANSFER,
                "Monthly salary"
        );

        TransactionResponse newTransaction = new TransactionResponse(
                UUID.randomUUID(),
                accountId,
                new BigDecimal("150.00"),
                Currency.GBP,
                TransactionType.CREDIT,
                TransactionCategory.TRANSFER,
                "Monthly salary",
                TransactionStatus.COMPLETED,
                0,
                LocalDateTime.now()
        );

        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(newTransaction);

        // When/Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.category").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - Should return 400 for invalid request")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        // Given - Invalid request with negative amount
        String invalidRequest = """
                {
                    "accountId": "%s",
                    "amount": -100.00,
                    "type": "DEBIT",
                    "category": "PAYMENT",
                    "description": "Test"
                }
                """.formatted(accountId);

        // When/Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/transactions/flagged - Should return flagged transactions")
    void shouldReturnFlaggedTransactions() throws Exception {
        // Given
        TransactionResponse flaggedTransaction = new TransactionResponse(
                UUID.randomUUID(),
                accountId,
                new BigDecimal("15000.00"),
                Currency.GBP,
                TransactionType.DEBIT,
                TransactionCategory.PAYMENT,
                "Suspicious transaction",
                TransactionStatus.FLAGGED,
                85,
                LocalDateTime.now()
        );

        Page<TransactionResponse> flaggedPage = new PageImpl<>(
                List.of(flaggedTransaction),
                PageRequest.of(0, 20),
                1
        );

        when(transactionService.findFlagged(any(Pageable.class))).thenReturn(flaggedPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions/flagged")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("FLAGGED"))
                .andExpect(jsonPath("$.content[0].riskScore").value(85))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/flagged - Should use default pagination")
    void shouldUseDefaultPaginationForFlagged() throws Exception {
        // Given
        Page<TransactionResponse> emptyPage = new PageImpl<>(List.of());
        when(transactionService.findFlagged(any(Pageable.class))).thenReturn(emptyPage);

        // When/Then
        mockMvc.perform(get("/api/v1/transactions/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
