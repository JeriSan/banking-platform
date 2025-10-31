package com.bank.account.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    private String id;
    private String customerId;
    private AccountType type;
    private BigDecimal balance;
    private boolean maintenanceFee;
    private Integer monthlyMovementLimit;
    private Integer fixedTermDayOfMonth;
    private List<String> owners;
    private List<String> authorizedSigners;
    private Boolean business;
    private int movementCountThisMonth;
}
