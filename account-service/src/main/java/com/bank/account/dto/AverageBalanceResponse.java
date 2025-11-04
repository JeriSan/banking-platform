package com.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AverageBalanceResponse {
    private String productId;
    private java.time.YearMonth yearMonth;
    private java.math.BigDecimal averageDailyBalance;
}