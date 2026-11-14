
package com.codegoats.viralvault.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class UserAdminController {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserAdminController(
            UserRepository userRepository,
            PasswordService passwordService) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    // Only expose information needed by the admin interface.
    public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        boolean admin
    ) {}

    public record UpdateUserRequest(
        String fullName,
        String email,
        String password
    ) {}

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.isAdmin()
        );
    }

    private Long getUserId(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (userId instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    private ResponseEntity<?> checkAdmin(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        if (userId == null) {
            return ResponseEntity.status(401).body(
                Map.of("message",
                    "Administrator authentication required.")
            );
        }

        boolean admin = userRepository.findById(userId)
            .map(User::isAdmin)
            .orElse(false);

        if (!admin) {
            return ResponseEntity.status(403).body(
                Map.of("message",
                    "Administrator privileges required.")
            );
        }

        return null;
    }

    // Retrieve all registered users.
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        return ResponseEntity.ok(
            userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList()
        );
    }

    // Update customer information and optionally reset passwords.
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest body,
            HttpServletRequest request) {

        ResponseEntity<?> denied = checkAdmin(request);

        if (denied != null) {
            return denied;
        }

        var existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            return ResponseEntity.status(404).body(
                Map.of("message", "User not found.")
            );
        }

        User user = existingUser.get();

        // Protect administrator accounts.
        if (user.isAdmin()) {
            return ResponseEntity.status(403).body(
                Map.of("message",
                    "Administrator accounts cannot be edited here.")
            );
        }

        if (body == null) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Update information is required.")
            );
        }

        String fullName = body.fullName() == null
            ? null : body.fullName().trim();

        String email = body.email() == null
            ? null : body.email().trim();

        String password = body.password();

        if (fullName != null &&
                (fullName.isBlank() || fullName.length() > 100)) {

            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Name must contain between 1 and 100 characters.")
            );
        }

        if (email != null) {

            if (email.length() > 100 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

                return ResponseEntity.badRequest().body(
                    Map.of("message",
                        "Please enter a valid email address.")
                );
            }

            if (userRepository.emailUsedByAnotherUser(email, id)) {
                return ResponseEntity.status(409).body(
                    Map.of("message",
                        "This email is already registered.")
                );
            }
        }

        String passwordHash = null;

        if (password != null && !password.isEmpty()) {

            if (password.isBlank() ||
                password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {

                return ResponseEntity.badRequest().body(
                    Map.of("message",
                        "Password must contain at least 8 characters "
                        + "and cannot exceed 72 UTF-8 bytes.")
                );
            }

            passwordHash = passwordService.hashPassword(password);
        }

        try {

            int updated = userRepository.updateUserByAdmin(
                id,
                fullName,
                email,
                passwordHash
            );

            if (updated == 0) {
                return ResponseEntity.status(409).body(
                    Map.of("message",
                        "The user could not be updated.")
                );
            }

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409).body(
                Map.of("message",
                    "The update conflicts with existing user information.")
            );
        }

        var updatedUser = userRepository.findById(id);

        if (updatedUser.isEmpty()) {
            return ResponseEntity.status(404).body(
                Map.of("message", "User not found.")
            );
        }

        return ResponseEntity.ok(
            toResponse(updatedUser.get())
        );
    }
}
