package net.matheodrd.bankapi.controller;

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
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PageResponse<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.debug("GET /api/v1/accounts - page: {}, size: {}, sort: {}", page, size, sort);

        Pageable pageable = createPageable(page, size, sort);
        Page<AccountResponse> accounts = accountService.findAll(pageable);

        return ResponseEntity.ok(PageResponse.from(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailResponse> getAccountById(
            @PathVariable UUID id
    ) {
        log.debug("GET /api/v1/accounts/{}", id);

        AccountDetailResponse account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        log.info("POST /api/v1/accounts - Creating account for: {}", request.accountHolder());

        AccountResponse createdAccount = accountService.create(request);

        URI location = URI.create("/api/v1/accounts/" + createdAccount.id());
        return ResponseEntity.created(location).body(createdAccount);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable UUID id,

            @Valid @RequestBody UpdateAccountStatusRequest request
    ) {
        log.info("PATCH /api/v1/accounts/{}/status - New status: {}", id, request.status());

        AccountResponse updatedAccount = accountService.updateStatus(id, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<PageResponse<TransactionResponse>> getAccountTransactions(
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
