package com.bank.debitcard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    private String id;
    private String customerId;
    private BigDecimal balance;
}