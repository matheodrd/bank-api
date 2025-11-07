package net.matheodrd.bankapi.controller;

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
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) UUID accountId,

            @RequestParam(required = false) TransactionStatus status,

            @RequestParam(required = false) TransactionType type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,

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
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID id
    ) {
        log.debug("GET /api/v1/transactions/{}", id);

        TransactionResponse transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
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
