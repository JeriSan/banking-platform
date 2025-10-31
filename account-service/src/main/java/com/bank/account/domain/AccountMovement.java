package com.bank.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Document("account_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountMovement {
    @Id
    private String id;
    private String accountId;
    private LocalDateTime timestamp;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String note;
}
