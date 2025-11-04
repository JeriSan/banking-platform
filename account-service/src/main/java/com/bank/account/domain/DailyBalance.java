package com.bank.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("daily_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBalance {
    @Id
    private String id;
    private String productId; // accountId
    private LocalDateTime date;
    private BigDecimal balance;
}
