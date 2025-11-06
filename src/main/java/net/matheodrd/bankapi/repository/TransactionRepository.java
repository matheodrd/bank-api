package net.matheodrd.bankapi.repository;

import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Transactions by Account
    Page<Transaction> findByAccountIdOrderByTimestampDesc(
            UUID accountId,
            Pageable pageable
    );

    // Flagged transactions
    Page<Transaction> findByStatusOrderByRiskScoreDesc(
            TransactionStatus status,
            Pageable pageable
    );

    // For risk score calculation : account recent transactions
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId " +
            "AND t.timestamp > :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentByAccountId(
            @Param("accountId") UUID accountId,
            @Param("since") LocalDateTime since
    );

    @Query("""
                SELECT t FROM Transaction t
                WHERE (:accountId IS NULL OR t.accountId = :accountId)
                AND (:status IS NULL OR t.status = :status)
                AND (:type IS NULL OR t.type = :type)
                AND (:fromDate IS NULL OR t.timestamp >= :fromDate)
                AND (:toDate IS NULL OR t.timestamp <= :toDate)
                ORDER BY t.timestamp DESC
            """)
    Page<Transaction> findByFilters(
            @Param("accountId") UUID accountId,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

}
