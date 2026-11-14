
package com.codegoats.viralvault.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    private static final String ADMIN_USERNAME = "ViralVaultAdmin";
    private static final String ADMIN_EMAIL = "admin@viralvault.test";

    @Value("${VIRALVAULT_ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminInitializer(
            JdbcTemplate jdbcTemplate,
            UserRepository userRepository,
            PasswordService passwordService) {

        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Check whether the administrator already exists.
        var existingUser =
                userRepository.findByUsername(ADMIN_USERNAME);

        if (existingUser.isPresent()) {

            if (existingUser.get().isAdmin()
                    && ADMIN_EMAIL.equals(
                        existingUser.get().getEmail())) {

                System.out.println(
                    "[Viral Vault] Administrator account already exists."
                );

            } else {

                System.out.println(
                    "[Viral Vault] Admin username is already occupied."
                );
            }

            return;
        }

        // Do not create an account without a configured password.
        if (adminPassword == null || adminPassword.isBlank()) {

            System.out.println(
                "[Viral Vault] Admin password not configured."
            );

            return;
        }

        if (adminPassword.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            ).length > 72) {

            throw new IllegalStateException(
                "Admin password cannot exceed 72 UTF-8 bytes."
            );
        }

        // Prevent conflicts with existing accounts.
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {

            System.out.println(
                "[Viral Vault] Admin email is already occupied."
            );

            return;
        }

        // Securely hash the administrator password.
        String passwordHash =
                passwordService.hashPassword(adminPassword);

        // Create the administrator directly in the database.
        String sql = """
            INSERT INTO users (
                username,
                full_name,
                email,
                phone_number,
                password_hash,
                is_admin
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            ADMIN_USERNAME,
            "Viral Vault Administrator",
            ADMIN_EMAIL,
            "0000000000",
            passwordHash,
            1
        );

        System.out.println(
            "[Viral Vault] Administrator account created successfully."
        );
    }
}
