package com.neobank.neobank_backend.controller;

import com.neobank.neobank_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/check-password")
    public String checkPassword(
            @RequestParam String email,
            @RequestParam String password
    ) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "❌ Utilisateur non trouvé: " + email;
        }

        boolean matches = encoder.matches(password, user.getPasswordHash());

        return "📧 Email: " + email +
                "\n🔑 Hash en base: " + user.getPasswordHash() +
                "\n🔐 Mot de passe testé: " + password +
                "\n✅ Correspond: " + matches;
    }
}