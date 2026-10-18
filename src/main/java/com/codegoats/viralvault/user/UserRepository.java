
package com.codegoats.viralvault.user;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a new user to the SQLite database
    public int save(User user) {

        String sql = """
            INSERT INTO users
            (username, full_name, email, phone_number, password_hash)
            VALUES (?, ?, ?, ?, ?)
            """;

        return jdbcTemplate.update(
            sql,
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getPasswordHash()
        );
    }

    // Check whether an email is already registered
    public boolean existsByEmail(String email) {

        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            email
        );

        return count != null && count > 0;
    }

    // Check whether a username is already taken
    public boolean existsByUsername(String username) {

        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            username
        );

        return count != null && count > 0;
    }

    // Find an existing user by email for login
    public Optional<User> findByEmail(String email) {

        String sql = """
            SELECT user_id, username, full_name, email,
                   phone_number, password_hash, is_admin
            FROM users
            WHERE email = ?
            """;

        List<User> users = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                User user = new User();

                user.setId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setAdmin(rs.getBoolean("is_admin"));

                return user;
            },
            email
        );

        return users.stream().findFirst();
    }
}
