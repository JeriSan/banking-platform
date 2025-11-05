package com.bank.debitcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("debit_card_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitCardMovement {
    @Id
    private String id;
    private String cardId;
    private LocalDateTime timestamp;
    private String type;            // PAYMENT | WITHDRAW
    private BigDecimal amount;
    private String accountUsedId;   // desde qué cuenta se descontó
    private String description;
}