package com.paypalclone.featheredoofbird.payments.infrastructure.persistence;

import com.paypalclone.featheredoofbird.payments.domain.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDocument {

    @Id private Long id;

    private String sender;

    private String receiver;

    private BigDecimal amount;

    private String currency;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Transaction.TransactionStatus status;
}
