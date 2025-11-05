package net.matheodrd.bankapi.model;

import jakarta.persistence.*;
import lombok.*;
import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.model.enums.TransactionCategory;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import net.matheodrd.bankapi.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TransactionCategory category;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}