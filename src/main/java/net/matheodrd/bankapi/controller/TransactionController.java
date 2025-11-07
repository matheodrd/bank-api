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
import net.matheodrd.bankapi.dto.request.CreateTransactionRequest;
import net.matheodrd.bankapi.dto.response.PageResponse;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;
import net.matheodrd.bankapi.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve a paginated and filtered list of transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(
            @Parameter(description = "Filter by account ID")
            @RequestParam(required = false) UUID accountId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) TransactionStatus status,

            @Parameter(description = "Filter by type (DEBIT/CREDIT)")
            @RequestParam(required = false) TransactionType type,

            @Parameter(description = "Filter from date (ISO format: 2025-01-01T10:00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,

            @Parameter(description = "Filter to date (ISO format: 2025-01-31T23:59:59)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/transactions - Filters: accountId={}, status={}, type={}, fromDate={}, toDate={}",
                accountId, status, type, fromDate, toDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.findByFilters(
                accountId, status, type, fromDate, toDate, pageable
        );

        return ResponseEntity.ok(PageResponse.from(transactions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve details of a specific transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction UUID")
            @PathVariable UUID id
    ) {
        log.debug("GET /api/v1/transactions/{}", id);

        TransactionResponse transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    @Operation(
            summary = "Create a new transaction",
            description = "Create a new transaction. The risk score is calculated automatically and the transaction may be flagged."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Account suspended"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        log.info("POST /api/v1/transactions - Creating {} transaction of {} for account {}",
                request.type(), request.amount(), request.accountId());

        TransactionResponse createdTransaction = transactionService.create(request);

        URI location = URI.create("/api/v1/transactions/" + createdTransaction.id());
        return ResponseEntity.created(location).body(createdTransaction);
    }

    @GetMapping("/flagged")
    @Operation(
            summary = "Get flagged transactions",
            description = "Retrieve all transactions flagged as suspicious (risk score > 70)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved flagged transactions")
    })
    public ResponseEntity<PageResponse<TransactionResponse>> getFlaggedTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/transactions/flagged - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> flaggedTransactions = transactionService.findFlagged(pageable);

        return ResponseEntity.ok(PageResponse.from(flaggedTransactions));
    }
}
