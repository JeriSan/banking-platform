package com.bank.account.dto;

import com.bank.account.domain.FeeMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeesReportResponse {
    private String accountId;
    private LocalDateTime from;
    private LocalDateTime to;
    private BigDecimal totalFees;
    private List<FeeMovement> details;
}