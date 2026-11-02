package com.neobank.neobank_backend.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Le numéro de compte du destinataire est obligatoire")
    private String destinationAccountNumber;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.00", message = "Le montant minimum est de 1 FCFA")
    private BigDecimal amount;

    private String description;
}