package com.neobank.neobank_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String message;
    private boolean success;
    private String email;
    private String firstName;
    private String lastName;

    public static LoginResponse success(String token, String email, String firstName, String lastName) {
        return LoginResponse.builder()
                .success(true)
                .token(token)
                .message("Connexion réussie")
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static LoginResponse error(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}