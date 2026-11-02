package com.neobank.neobank_backend.user.service;

import com.neobank.neobank_backend.exception.EmailAlreadyExistsException;
import com.neobank.neobank_backend.exception.PhoneAlreadyExistsException;
import com.neobank.neobank_backend.user.dto.PasswordUpdateRequest;
import com.neobank.neobank_backend.user.dto.ProfileResponse;
import com.neobank.neobank_backend.user.dto.ProfileUpdateRequest;
import com.neobank.neobank_backend.user.entity.Account;
import com.neobank.neobank_backend.user.entity.User;
import com.neobank.neobank_backend.user.repository.AccountRepository;
import com.neobank.neobank_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .accountNumber(account.getAccountNumber())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Vérifier si le téléphone est déjà utilisé par un autre utilisateur
        if (!user.getPhone().equals(request.getPhone()) &&
                userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
        }

        // Mettre à jour les informations
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        userRepository.save(user);

        // Récupérer le compte pour la réponse
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        return ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .accountNumber(account.getAccountNumber())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        // Encoder et mettre à jour le nouveau mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }



}