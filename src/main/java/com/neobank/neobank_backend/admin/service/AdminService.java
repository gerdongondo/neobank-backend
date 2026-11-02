package com.neobank.neobank_backend.admin.service;

import com.neobank.neobank_backend.admin.dto.AdminStatsResponse;
import com.neobank.neobank_backend.admin.dto.TransactionAdminResponse;
import com.neobank.neobank_backend.admin.dto.UserAdminResponse;
import com.neobank.neobank_backend.user.entity.Account;
import com.neobank.neobank_backend.user.entity.Transaction;
import com.neobank.neobank_backend.user.entity.User;
import com.neobank.neobank_backend.user.repository.AccountRepository;
import com.neobank.neobank_backend.user.repository.TransactionRepository;
import com.neobank.neobank_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Récupérer tous les utilisateurs avec leurs comptes
    public Page<UserAdminResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserAdminResponse);
    }

    // Récupérer un utilisateur par ID
    public UserAdminResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapToUserAdminResponse(user);
    }

    // Bloquer un compte
    @Transactional
    public void blockAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        account.setStatus(Account.AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    // Débloquer un compte
    @Transactional
    public void unblockAccount(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    // Récupérer toutes les transactions avec DTO
    public Page<TransactionAdminResponse> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        List<TransactionAdminResponse> responses = transactions.getContent().stream()
                .map(this::mapToTransactionAdminResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, transactions.getTotalElements());
    }

    // Statistiques
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Transaction> transactions = transactionRepository.findAll();
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.TransactionType.TRANSFER_OUT) {
                totalAmount = totalAmount.add(t.getAmount());
            }
        }

        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getAccount() != null && u.getAccount().getStatus() == Account.AccountStatus.ACTIVE)
                .count();

        long blockedAccounts = accountRepository.findAll().stream()
                .filter(a -> a.getStatus() == Account.AccountStatus.BLOCKED)
                .count();

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalAccounts(totalAccounts)
                .totalTransactions(totalTransactions)
                .totalAmount(totalAmount)
                .activeUsers(activeUsers)
                .blockedAccounts(blockedAccounts)
                .build();
    }

    // Recherche d'utilisateurs par email ou nom
    public List<UserAdminResponse> searchUsers(String keyword) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getEmail().contains(keyword) ||
                        u.getFirstName().contains(keyword) ||
                        u.getLastName().contains(keyword))
                .collect(Collectors.toList());
        return users.stream()
                .map(this::mapToUserAdminResponse)
                .collect(Collectors.toList());
    }

    // Mapper Transaction -> TransactionAdminResponse
    private TransactionAdminResponse mapToTransactionAdminResponse(Transaction transaction) {
        Account sourceAccount = transaction.getSourceAccount();
        Account destinationAccount = transaction.getDestinationAccount();

        User sourceUser = sourceAccount.getUser();
        User destinationUser = destinationAccount.getUser();

        return TransactionAdminResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .sourceUserEmail(sourceUser.getEmail())
                .destinationUserEmail(destinationUser.getEmail())
                .sourceUserName(sourceUser.getFirstName() + " " + sourceUser.getLastName())
                .destinationUserName(destinationUser.getFirstName() + " " + destinationUser.getLastName())
                .build();
    }

    // Mapper User -> UserAdminResponse
    private UserAdminResponse mapToUserAdminResponse(User user) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);

        return UserAdminResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .accountNumber(account != null ? account.getAccountNumber() : null)
                .balance(account != null ? account.getBalance() : BigDecimal.ZERO)
                .status(account != null ? account.getStatus().name() : "NO_ACCOUNT")
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}