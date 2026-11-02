package com.neobank.neobank_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String firstName;
    private String lastName;
    private String email;
}