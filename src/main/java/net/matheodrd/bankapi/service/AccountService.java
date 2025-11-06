package net.matheodrd.bankapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.matheodrd.bankapi.dto.request.CreateAccountRequest;
import net.matheodrd.bankapi.dto.request.UpdateAccountStatusRequest;
import net.matheodrd.bankapi.dto.response.AccountDetailResponse;
import net.matheodrd.bankapi.dto.response.AccountResponse;
import net.matheodrd.bankapi.exception.EntityNotFoundException;
import net.matheodrd.bankapi.mapper.AccountMapper;
import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.model.enums.AccountStatus;
import net.matheodrd.bankapi.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public Page<AccountResponse> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(accountMapper::toResponse);
    }

    public AccountDetailResponse findById(UUID id) {
        return accountRepository.findDetailById(id)
                .map(accountMapper::toDetailResponse)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        String accountNumber = generateAccountNumber();

        Account account = accountMapper.toEntityWithDefaults(request, accountNumber);

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());

        return accountMapper.toResponse(saved);
    }

    @Transactional
    public AccountResponse updateStatus(UUID id, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + id));

        AccountStatus oldStatus = account.getStatus();
        account.setStatus(request.status());

        log.info("Account {} status changed: {} -> {}",
                account.getAccountNumber(), oldStatus, request.status());

        return accountMapper.toResponse(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            BigInteger max = new BigInteger("9999999999999999999999");
            BigInteger randomBigInt = new BigInteger(max.bitLength(), new java.util.Random());
            randomBigInt = randomBigInt.mod(max.add(BigInteger.ONE));

            number = "GB" + String.format("%022d", randomBigInt);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
