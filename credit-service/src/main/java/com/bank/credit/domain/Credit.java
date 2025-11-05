package com.bank.credit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document("credits")
@Data
@AllArgsConstructor
@Builder

public class Credit {
    @Id
    private String id;

    private String customerId;

    private CreditType type;

    private BigDecimal principal;

    private BigDecimal balance;

    private BigDecimal interestRateAnnual; // simple example

    private boolean active;

    private LocalDateTime startDate;

    private LocalDateTime nextDueDate; //proxima fecha de pago programada
    private boolean overdue; // bandera calculada actualizada por job o al pagar
}
