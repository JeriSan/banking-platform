package com.bank.debitcard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("debit_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitCard {
    @Id
    private String id;
    private String customerId;
    private String primaryAccountId;
    private List<String> linkedAccountIds;
    private boolean active;
}