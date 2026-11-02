package com.neobank.neobank_backend.transfer.service;

import com.neobank.neobank_backend.transfer.dto.TransferRequest;
import com.neobank.neobank_backend.user.entity.Account;
import com.neobank.neobank_backend.user.entity.Transaction;
import com.neobank.neobank_backend.user.entity.User;
import com.neobank.neobank_backend.user.repository.AccountRepository;
import com.neobank.neobank_backend.user.repository.TransactionRepository;
import com.neobank.neobank_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transaction executeTransfer(String userEmail, TransferRequest request) {
        // 1. Récupérer l'utilisateur connecté
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Récupérer le compte source
        Account sourceAccount = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte source non trouvé"));

        // 🔥 VÉRIFIER SI LE COMPTE SOURCE EST BLOQUÉ
        if (sourceAccount.getStatus() == Account.AccountStatus.BLOCKED) {
            throw new RuntimeException("Votre compte est bloqué. Vous ne pouvez pas effectuer de transferts.");
        }

        // 3. Récupérer le compte destination
        Account destinationAccount = accountRepository.findByAccountNumberWithLock(
                request.getDestinationAccountNumber()
        ).orElseThrow(() -> new RuntimeException("Compte du destinataire non trouvé"));

        // 🔥 VÉRIFIER SI LE COMPTE DESTINATAIRE EST BLOQUÉ
        if (destinationAccount.getStatus() == Account.AccountStatus.BLOCKED) {
            throw new RuntimeException("Le compte du destinataire est bloqué.");
        }

        // 4. Vérifier qu'on ne transfère pas vers son propre compte
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new RuntimeException("Vous ne pouvez pas transférer vers votre propre compte");
        }

        // 5. Vérifier le solde
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Solde insuffisant");
        }

        // 6. Débiter le compte source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));

        // 7. Créditer le compte destination
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        // 8. Sauvegarder les comptes
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // 9. Créer la transaction
        Transaction debitTransaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER_OUT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Transfert vers " + destinationAccount.getAccountNumber())
                .build();

        // 10. Sauvegarder la transaction
        return transactionRepository.save(debitTransaction);
    }

    // Méthode pour récupérer les transactions d'un utilisateur
    public org.springframework.data.domain.Page<Transaction> getUserTransactions(String userEmail,
                                                                                 org.springframework.data.domain.Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        return transactionRepository.findTransactionsByAccountId(account.getId(), pageable);
    }
}