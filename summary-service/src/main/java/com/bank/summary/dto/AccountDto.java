package com.bank.summary.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private String id;
    private String customerId;
    private String accountType;         // SAVINGS, CURRENT, FIXED_TERM, etc.
    private BigDecimal balance;
    private BigDecimal minimumOpeningAmount;
    private Integer freeTransactionsPerMonth;
    private BigDecimal feePerExtraTransaction;
    private boolean active;

    // opcional — si tu account-service lo expone
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}