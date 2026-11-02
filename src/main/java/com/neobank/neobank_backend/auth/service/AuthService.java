package com.neobank.neobank_backend.auth.service;

import com.neobank.neobank_backend.auth.dto.LoginRequest;
import com.neobank.neobank_backend.auth.dto.RegisterRequest;
import com.neobank.neobank_backend.exception.EmailAlreadyExistsException;
import com.neobank.neobank_backend.exception.PhoneAlreadyExistsException;
import com.neobank.neobank_backend.security.jwt.JwtService;
import com.neobank.neobank_backend.user.entity.Account;                    // ← AJOUT
import com.neobank.neobank_backend.user.entity.User;
import com.neobank.neobank_backend.user.repository.AccountRepository;      // ← AJOUT
import com.neobank.neobank_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // ← AJOUT
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;   // ← AJOUT
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("XOF")
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .build();

        accountRepository.save(account);
    }

    public String login(LoginRequest request) {
        try {
            System.out.println("🔐 Tentative de login: " + request.getEmail());

            // 1. Authentifier (vérifie email + mot de passe)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 🔥 2. VÉRIFIER SI LE COMPTE EST BLOQUÉ
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Account account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

            if (account.getStatus() == Account.AccountStatus.BLOCKED) {
                System.out.println("⛔ Compte bloqué: " + request.getEmail());
                throw new RuntimeException("Votre compte est bloqué. Contactez votre administrateur.");
            }

            System.out.println("✅ Authentification réussie pour: " + request.getEmail());
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtService.generateToken(userDetails);

        } catch (BadCredentialsException e) {
            System.out.println("❌ Identifiants incorrects: " + request.getEmail());
            throw new RuntimeException("Les identifications sont erronées");
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }

    private String generateAccountNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        long random = (long) (Math.random() * 1_000_000);
        String randomPart = String.format("%06d", random);
        return "SN" + year + randomPart;
    }
}