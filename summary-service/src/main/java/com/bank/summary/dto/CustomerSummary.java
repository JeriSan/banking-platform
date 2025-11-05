package com.bank.summary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSummary {
    private String customerId;
    private List<AccountDto> accounts;
    private List<CreditDto> credits;
    private List<CreditCardDto> creditCards;
    private List<DebitCardDto> debitCards;
    private boolean hasOverdueDebt;
}