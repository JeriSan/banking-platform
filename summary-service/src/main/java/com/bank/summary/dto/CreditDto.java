package com.bank.summary.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditDto {
    private String id;
    private String customerId;
    private String type;           // PERSONAL, BUSINESS
    private BigDecimal principal;
    private BigDecimal balance;
    private boolean active;
    private LocalDate startDate;

    // Enfoque consolidado
    private LocalDate nextDueDate;
    private boolean overdue;
}