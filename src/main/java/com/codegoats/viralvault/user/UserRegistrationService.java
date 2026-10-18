
package com.codegoats.viralvault.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {

    private final UserValidationService validationService;
    private final PasswordService passwordService;
    private final UserRepository userRepository;

    public UserRegistrationService(
            UserValidationService validationService,
            PasswordService passwordService,
            UserRepository userRepository) {

        this.validationService = validationService;
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    // Validate and register a new user
    public List<String> registerUser(RegistrationRequest request) {

        List<String> errors = new ArrayList<>(
            validationService.validateRegistration(request)
        );

        // Stop if the registration information is invalid
        if (!errors.isEmpty()) {
            return errors;
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Check for existing accounts
        if (userRepository.existsByUsername(username)) {
            errors.add("Username is already taken.");
        }

        if (userRepository.existsByEmail(email)) {
            errors.add("Email is already registered.");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        // Create the user object
        User user = new User();
        user.setUsername(username);
        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber().trim());

        // Never store the original password
        user.setPasswordHash(
            passwordService.hashPassword(request.getPassword())
        );

        // Save the new account to SQLite
        userRepository.save(user);

        return errors;
    }
}
