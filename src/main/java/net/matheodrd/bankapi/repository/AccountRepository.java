package net.matheodrd.bankapi.repository;

import net.matheodrd.bankapi.model.Account;
import net.matheodrd.bankapi.repository.projection.AccountDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("""
                SELECT
                    a.id as id, a.accountNumber as accountNumber, a.accountHolder as accountHolder,
                    a.balance as balance, a.currency as currency, a.status as status,
                    a.createdAt as createdAt, a.updatedAt as updatedAt,
                    COUNT(t.id) as totalTransactions,
                    COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0.0 END), 0.0) as totalDebits,
                    COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0.0 END), 0.0) as totalCredits
                FROM Account a
                LEFT JOIN Transaction t ON t.accountId = a.id
                WHERE a.id = :accountId
                GROUP BY a.id, a.accountNumber, a.accountHolder, a.balance,
                         a.currency, a.status, a.createdAt, a.updatedAt
            """)
    Optional<AccountDetailProjection> findDetailById(@Param("accountId") UUID accountId);
}

