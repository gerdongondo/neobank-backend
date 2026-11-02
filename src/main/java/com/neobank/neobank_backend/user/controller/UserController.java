package com.neobank.neobank_backend.user.controller;

import com.neobank.neobank_backend.user.dto.PasswordUpdateRequest;
import com.neobank.neobank_backend.user.dto.ProfileResponse;
import com.neobank.neobank_backend.user.dto.ProfileUpdateRequest;
import com.neobank.neobank_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        ProfileResponse profile = userService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        String email = authentication.getName();
        ProfileResponse updatedProfile = userService.updateProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        String email = authentication.getName();
        userService.updatePassword(email, request);
        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe modifié avec succès",
                "success", "true"
        ));
    }
}