package com.bank.card.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("credit_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {
    @Id
    private String id;
    private String customerId;
    private CardOwnerType ownerType;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance; // used amount
    private boolean active;
}
