package com.neobank.neobank_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private long totalAccounts;
    private long activeUsers;
    private long blockedAccounts;
}