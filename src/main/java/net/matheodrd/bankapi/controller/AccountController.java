package net.matheodrd.bankapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.matheodrd.bankapi.dto.request.CreateAccountRequest;
import net.matheodrd.bankapi.dto.request.UpdateAccountStatusRequest;
import net.matheodrd.bankapi.dto.response.AccountDetailResponse;
import net.matheodrd.bankapi.dto.response.AccountResponse;
import net.matheodrd.bankapi.dto.response.PageResponse;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.service.AccountService;
import net.matheodrd.bankapi.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieve a paginated list of all accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<PageResponse<AccountResponse>> getAllAccounts(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field and direction (e.g., 'createdAt,desc')")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.debug("GET /api/v1/accounts - page: {}, size: {}, sort: {}", page, size, sort);

        Pageable pageable = createPageable(page, size, sort);
        Page<AccountResponse> accounts = accountService.findAll(pageable);

        return ResponseEntity.ok(PageResponse.from(accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieve detailed information about a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountDetailResponse> getAccountById(
            @Parameter(description = "Account UUID")
            @PathVariable UUID id
    ) {
        log.debug("GET /api/v1/accounts/{}", id);

        AccountDetailResponse account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    @Operation(summary = "Create a new account", description = "Create a new bank account with initial balance")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        log.info("POST /api/v1/accounts - Creating account for: {}", request.accountHolder());

        AccountResponse createdAccount = accountService.create(request);

        URI location = URI.create("/api/v1/accounts/" + createdAccount.id());
        return ResponseEntity.created(location).body(createdAccount);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update account status", description = "Change the status of an account (ACTIVE, SUSPENDED, CLOSED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status updated"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @Parameter(description = "Account UUID")
            @PathVariable UUID id,

            @Valid @RequestBody UpdateAccountStatusRequest request
    ) {
        log.info("PATCH /api/v1/accounts/{}/status - New status: {}", id, request.status());

        AccountResponse updatedAccount = accountService.updateStatus(id, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Get account transactions", description = "Retrieve all transactions for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<PageResponse<TransactionResponse>> getAccountTransactions(
            @Parameter(description = "Account UUID")
            @PathVariable UUID id,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/accounts/{}/transactions - page: {}, size: {}", id, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.findByAccountId(id, pageable);

        return ResponseEntity.ok(PageResponse.from(transactions));
    }

    private Pageable createPageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        String field = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
