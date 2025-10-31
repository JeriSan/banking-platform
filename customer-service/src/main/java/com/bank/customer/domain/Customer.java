package com.bank.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    private String id;
    private CustomerType type;
    private String documentType;
    private String documentNumber;
    private String fullName;
    private String businessName;
    private String email;
    private String phone;
    private boolean active;
}
