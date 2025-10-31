package com.bank.card.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Document("card_movements")
@Data
@AllArgsConstructor
@Builder
public class CardMovement {
    @Id

    private String id;

    private String cardId;

    private LocalDateTime timestamp;

    private String type; // CHARGE | PAYMENT

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String description;

}
