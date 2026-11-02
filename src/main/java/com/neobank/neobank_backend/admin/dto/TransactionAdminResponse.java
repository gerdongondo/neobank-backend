package com.neobank.neobank_backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAdminResponse {
    private Long id;
    private String reference;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String type;
    private String status;
    private String description;
    private LocalDateTime transactionDate;
    private String sourceUserEmail;
    private String destinationUserEmail;
    private String sourceUserName;
    private String destinationUserName;
}