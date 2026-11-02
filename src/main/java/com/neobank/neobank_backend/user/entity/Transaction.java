package com.neobank.neobank_backend.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ← AJOUTER CET IMPORT
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    @JsonIgnore  // ← AJOUTER CETTE ANNOTATION
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    @JsonIgnore  // ← AJOUTER CETTE ANNOTATION
    private Account destinationAccount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = TransactionStatus.PENDING;
        if (transactionDate == null) transactionDate = LocalDateTime.now();
        if (reference == null) reference = java.util.UUID.randomUUID().toString();
    }

    public enum TransactionType {
        TRANSFER_OUT,
        TRANSFER_IN
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}