package net.matheodrd.bankapi.mapper;

import net.matheodrd.bankapi.dto.request.CreateTransactionRequest;
import net.matheodrd.bankapi.dto.response.TransactionResponse;
import net.matheodrd.bankapi.model.Transaction;
import net.matheodrd.bankapi.model.enums.Currency;
import net.matheodrd.bankapi.model.enums.TransactionStatus;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransactionMapper {

    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    // Méthode custom pour construction complète
    default Transaction toEntityWithCalculatedFields(
            CreateTransactionRequest request,
            Currency currency,
            TransactionStatus status,
            Integer riskScore,
            LocalDateTime timestamp
    ) {
        Transaction transaction = toEntity(request);
        transaction.setCurrency(currency);
        transaction.setStatus(status);
        transaction.setRiskScore(riskScore);
        transaction.setTimestamp(timestamp);
        return transaction;
    }
}
