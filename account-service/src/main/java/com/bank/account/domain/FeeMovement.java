package com.bank.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("fee_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeMovement {
    @Id
    private String id;
    private String accountId;
    private java.time.LocalDateTime timestamp;
    private java.math.BigDecimal amount;
    private String reason;
}
