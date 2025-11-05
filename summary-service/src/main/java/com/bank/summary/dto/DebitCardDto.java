package com.bank.summary.dto;

import lombok.Data;

import java.util.List;

@Data
public class DebitCardDto {
    private String id;
    private String customerId;
    private String primaryAccountId;   // cuenta principal
    private List<String> linkedAccountIds; // otras cuentas asociadas
    private boolean active;
}