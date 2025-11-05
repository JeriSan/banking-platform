package com.bank.summary.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardDto {
    private String id;
    private String customerId;
    private BigDecimal creditLimit;
    private BigDecimal availableBalance;
    private boolean active;
}