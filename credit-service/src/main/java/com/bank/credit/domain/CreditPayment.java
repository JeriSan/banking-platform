package com.bank.credit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Document("credit_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditPayment {
    @Id

    private String id;

    private String creditId;

    private LocalDateTime timestamp;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

}
