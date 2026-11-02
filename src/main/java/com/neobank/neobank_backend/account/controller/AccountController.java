package com.neobank.neobank_backend.account.controller;


import com.neobank.neobank_backend.auth.dto.AccountResponse;
import com.neobank.neobank_backend.user.entity.Account;
import com.neobank.neobank_backend.user.entity.User;
import com.neobank.neobank_backend.user.repository.AccountRepository;
import com.neobank.neobank_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getAccount(Authentication authentication) {
        // 1. Récupérer l'utilisateur connecté
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Récupérer son compte
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        // 3. Construire la réponse
        AccountResponse response = AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        return ResponseEntity.ok(Map.of(
                "balance", account.getBalance(),
                "currency", account.getCurrency()
        ));
    }
}