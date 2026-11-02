package com.neobank.neobank_backend.transfer.controller;

import com.neobank.neobank_backend.transfer.dto.TransferRequest;
import com.neobank.neobank_backend.transfer.dto.TransferResponse;
import com.neobank.neobank_backend.transfer.service.TransferService;
import com.neobank.neobank_backend.user.entity.Account;
import com.neobank.neobank_backend.user.entity.Transaction;
import com.neobank.neobank_backend.user.repository.AccountRepository;
import com.neobank.neobank_backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request
    ) {
        String userEmail = authentication.getName();

        try {
            Transaction transaction = transferService.executeTransfer(userEmail, request);

            // Récupérer le nouveau solde
            Account account = accountRepository.findByUserId(
                    userRepository.findByEmail(userEmail).get().getId()
            ).get();

            return ResponseEntity.ok(
                    TransferResponse.success(
                            transaction.getReference(),
                            account.getBalance(),
                            transaction.getId().toString()
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(TransferResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions = transferService.getUserTransactions(userEmail, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<Transaction> getTransactionDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        // Vérifier que l'utilisateur a accès à cette transaction
        String userEmail = authentication.getName();
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        var account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        var transaction = transferService.getUserTransactions(userEmail, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        return ResponseEntity.ok(transaction);
    }
}