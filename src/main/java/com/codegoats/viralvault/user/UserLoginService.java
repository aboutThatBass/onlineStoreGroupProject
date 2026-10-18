
package com.codegoats.viralvault.user;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserLoginService(
            UserRepository userRepository,
            PasswordService passwordService) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    // Verify a user's email and password
    public boolean login(String email, String password) {

        // Reject missing login information
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        // Normalize email for consistent lookup
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        // Find the account in SQLite
        Optional<User> existingUser =
                userRepository.findByEmail(normalizedEmail);

        if (existingUser.isEmpty()) {
            return false;
        }

        // Compare the entered password with the stored BCrypt hash
        return passwordService.verifyPassword(
                password,
                existingUser.get().getPasswordHash()
        );
    }
}
