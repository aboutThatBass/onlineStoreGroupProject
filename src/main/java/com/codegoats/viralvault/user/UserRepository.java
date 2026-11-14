
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


    public Optional<User> findByUsername(String username) {

        String sql = """
            SELECT user_id, username, full_name, email,
                phone_number, password_hash, is_admin
            FROM users
            WHERE username = ?
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
            username
        );

        return users.stream().findFirst();
    }

    public Optional<User> findById(Long userId) {
        String sql = """
            SELECT user_id, username, full_name, email,
                   phone_number, address, city, state,
                   zip_code, is_admin
            FROM users
            WHERE user_id = ?
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
                user.setAddress(rs.getString("address"));
                user.setCity(rs.getString("city"));
                user.setState(rs.getString("state"));
                user.setZipCode(rs.getString("zip_code"));
                user.setAdmin(rs.getBoolean("is_admin"));

                return user;
            },
            userId
        );

        return users.stream().findFirst();
    }

    public boolean emailUsedByAnotherUser(
            String email, Long userId) {

        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM users
            WHERE LOWER(email) = LOWER(?)
              AND user_id <> ?
            """,
            Integer.class,
            email,
            userId
        );

        return count != null && count > 0;
    }

    public int updateProfile(User user) {
        String sql = """
            UPDATE users
            SET full_name = ?,
                email = ?,
                phone_number = ?,
                address = ?,
                city = ?,
                state = ?,
                zip_code = ?
            WHERE user_id = ?
            """;

        return jdbcTemplate.update(
            sql,
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getCity(),
            user.getState(),
            user.getZipCode(),
            user.getId()
        );
    }

}
