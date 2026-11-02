package com.neobank.neobank_backend.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String message;
    private boolean success;
    private String transactionReference;
    private BigDecimal newBalance;
    private String transactionId;

    public static TransferResponse success(String transactionReference, BigDecimal newBalance, String transactionId) {
        return TransferResponse.builder()
                .success(true)
                .message("Transfert effectué avec succès")
                .transactionReference(transactionReference)
                .newBalance(newBalance)
                .transactionId(transactionId)
                .build();
    }

    public static TransferResponse error(String message) {
        return TransferResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}