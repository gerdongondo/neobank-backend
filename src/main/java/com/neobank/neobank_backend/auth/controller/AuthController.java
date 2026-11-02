package com.neobank.neobank_backend.auth.controller;

import com.neobank.neobank_backend.auth.dto.AuthResponse;
import com.neobank.neobank_backend.auth.dto.LoginRequest;
import com.neobank.neobank_backend.auth.dto.LoginResponse;
import com.neobank.neobank_backend.auth.dto.RegisterRequest;
import com.neobank.neobank_backend.auth.service.AuthService;
import com.neobank.neobank_backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthResponse.success("Utilisateur créé avec succès"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            User user = authService.getUserByEmail(request.getEmail());

            return ResponseEntity.ok(
                    LoginResponse.success(
                            token,
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName()
                    )
            );
        } catch (RuntimeException e) {
            String message = e.getMessage();

            // 🔥 Si le compte est bloqué → 403
            if (message != null && message.toLowerCase().contains("bloqué")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(LoginResponse.error(message));
            }

            // Sinon identifiants erronés → 401
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.error("Les identifications sont erronées"));
        }
    }
}