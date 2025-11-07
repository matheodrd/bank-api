package net.matheodrd.bankapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.matheodrd.bankapi.dto.request.CreateTransactionRequest;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.exception.AccountSuspendedException;
import net.matheodrd.bankapi.exception.EntityNotFoundException;
import net.matheodrd.bankapi.exception.InsufficientBalanceException;
import net.matheodrd.bankapi.mapper.TransactionMapper;
import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;
import net.matheodrd.bankapi.repository.AccountRepository;
import net.matheodrd.bankapi.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RiskCalculationService riskCalculationService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        // Account validation
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Account is suspended");
        }

        // Balance validation for DEBIT
        if (request.type() == TransactionType.DEBIT) {
            if (account.getBalance().compareTo(request.amount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
        }

        // Calculate risk score
        LocalDateTime now = LocalDateTime.now();
        int riskScore = riskCalculationService.calculateRiskScore(
                request.accountId(),
                request.amount(),
                now
        );

        TransactionStatus status = riskCalculationService.determineStatus(riskScore);

        // Create transaction
        Transaction transaction = transactionMapper.toEntityWithCalculatedFields(
                request,
                account.getCurrency(),
                status,
                riskScore,
                now
        );

        Transaction saved = transactionRepository.save(transaction);

        // Update balance if COMPLETED
        if (status == TransactionStatus.COMPLETED) {
            updateAccountBalance(account, request.amount(), request.type());
        }

        log.info("Transaction created: {} {} {} (risk: {})",
                request.type(), request.amount(), account.getCurrency(), riskScore);

        return transactionMapper.toResponse(saved);
    }

    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> findByFilters(
            UUID accountId,
            TransactionStatus status,
            TransactionType type,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        return transactionRepository.findByFilters(accountId, status, type, fromDate, toDate, pageable)
                .map(transactionMapper::toResponse);
    }

    public TransactionResponse findById(UUID id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + id));
    }

    public Page<TransactionResponse> findByAccountId(UUID accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId, pageable)
                .map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> findFlagged(Pageable pageable) {
        return transactionRepository.findByStatusOrderByRiskScoreDesc(
                TransactionStatus.FLAGGED,
                pageable
        ).map(transactionMapper::toResponse);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, TransactionType type) {
        BigDecimal newBalance = type == TransactionType.DEBIT
                ? account.getBalance().subtract(amount)
                : account.getBalance().add(amount);
        account.setBalance(newBalance);
    }
}
