
package com.codegoats.viralvault;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    // Create a temporary SQLite database for each test
    private Connection createTestDatabase(Path databasePath) throws Exception {

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databasePath.toAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    full_name VARCHAR(100),
                    email VARCHAR(255) NOT NULL UNIQUE,
                    phone_number VARCHAR(30),
                    password_hash VARCHAR(255) NOT NULL,
                    is_admin BOOLEAN DEFAULT 0
                )
                """);
        }

        return connection;
    }

    @Test
    void canReadUsersFromSQLite() throws Exception {

        Path databasePath = Files.createTempFile(
                "viralvault-test-", ".db"
        );

        try (Connection connection = createTestDatabase(databasePath);
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT COUNT(*) FROM users")) {

            assertTrue(results.next());
            assertEquals(0, results.getInt(1));

            System.out.println("SQLite connection successful!");
        } finally {
            Files.deleteIfExists(databasePath);
        }
    }

    @Test
    void phoneNumberColumnExists() throws Exception {

        Path databasePath = Files.createTempFile(
                "viralvault-test-", ".db"
        );

        try (Connection connection = createTestDatabase(databasePath);
             Statement statement = connection.createStatement();
             ResultSet columns =
                     statement.executeQuery("PRAGMA table_info(users)")) {

            boolean found = false;

            while (columns.next()) {
                if ("phone_number".equals(columns.getString("name"))) {
                    found = true;
                    break;
                }
            }

            assertTrue(
                    found,
                    "The users table should have a phone_number column"
            );
        } finally {
            Files.deleteIfExists(databasePath);
        }
    }

    @Test
    void registeredUserHasHashedPassword() throws Exception {

        Path databasePath = Files.createTempFile(
                "viralvault-test-", ".db"
        );

        try (Connection connection = createTestDatabase(databasePath)) {

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            String passwordHash = encoder.encode("TestPassword123!");

            String insertSql = """
                INSERT INTO users
                (username, full_name, email, phone_number, password_hash)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setString(1, "testuser123");
                statement.setString(2, "Test User");
                statement.setString(3, "testuser123@example.com");
                statement.setString(4, "2105551234");
                statement.setString(5, passwordHash);

                statement.executeUpdate();
            }

            String selectSql = """
                SELECT username, email, phone_number, password_hash
                FROM users
                WHERE username = ?
                """;

            try (PreparedStatement statement =
                         connection.prepareStatement(selectSql)) {

                statement.setString(1, "testuser123");

                try (ResultSet results = statement.executeQuery()) {

                    assertTrue(results.next());

                    assertEquals(
                            "testuser123",
                            results.getString("username")
                    );

                    assertEquals(
                            "testuser123@example.com",
                            results.getString("email")
                    );

                    assertEquals(
                            "2105551234",
                            results.getString("phone_number")
                    );

                    String storedHash = results.getString("password_hash");

                    assertNotNull(storedHash);
                    assertTrue(storedHash.startsWith("$2"));

                    assertTrue(
                            encoder.matches("TestPassword123!", storedHash),
                            "Stored hash should match the original password"
                    );
                }
            }

            System.out.println("Password hashing test successful!");

        } finally {
            Files.deleteIfExists(databasePath);
        }
    }
}
