
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

    @Value("${VIRALVAULT_LOCAL_DEV:true}")
    private boolean localDevelopment;

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

        // Use the shared password only during local development.
        if (adminPassword == null || adminPassword.isBlank()) {

            if (localDevelopment) {
                adminPassword = "ViralAdmin123";
            } else {
                System.out.println(
                    "[Viral Vault] Admin password not configured."
                );
                return;
            }
        }

        if (adminPassword.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            ).length > 72) {

            throw new IllegalStateException(
                "Admin password cannot exceed 72 UTF-8 bytes."
            );
        }

        // Check whether the administrator already exists.
        var existingUser =
                userRepository.findByUsername(ADMIN_USERNAME);

        if (existingUser.isPresent()) {

            if (existingUser.get().isAdmin()
                    && ADMIN_EMAIL.equals(
                        existingUser.get().getEmail())) {

                // Standardize the password for local development.
                if (localDevelopment) {

                    String passwordHash =
                            passwordService.hashPassword(adminPassword);

                    jdbcTemplate.update(
                        """
                        UPDATE users
                        SET password_hash = ?
                        WHERE username = ?
                        AND email = ?
                        AND is_admin = 1
                        """,
                        passwordHash,
                        ADMIN_USERNAME,
                        ADMIN_EMAIL
                    );

                    System.out.println(
                        "[Viral Vault] Local administrator account is ready."
                    );

                } else {

                    System.out.println(
                        "[Viral Vault] Administrator account already exists."
                    );
                }

            } else {

                System.out.println(
                    "[Viral Vault] Admin username is already occupied."
                );
            }

            return;
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
