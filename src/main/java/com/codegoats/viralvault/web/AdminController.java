
package com.codegoats.viralvault.web;

import java.util.Map;
import java.util.Optional;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.PasswordService;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AdminController(
            UserRepository userRepository,
            PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public record AdminLoginRequest(
            String username,
            String password) {
    }

    // Log in using administrator username and password.
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {

        if (request == null
                || request.username() == null
                || request.username().isBlank()
                || request.password() == null
                || request.password().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Username and password are required."
                    )
            );
        }

        String username = request.username().trim();

        Optional<User> existingUser =
                userRepository.findByUsername(username);

        // Return the same error for unknown users,
        // incorrect passwords, and non-admin accounts.
        if (existingUser.isEmpty()) {
            return invalidCredentials();
        }

        User admin = existingUser.get();

        if (!admin.isAdmin()
                || !passwordService.verifyPassword(
                        request.password(),
                        admin.getPasswordHash())) {
            return invalidCredentials();
        }

        // Prevent session fixation.
        HttpSession previousSession =
                httpRequest.getSession(false);

        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session =
                httpRequest.getSession(true);

        session.setAttribute("userId", admin.getId());
        session.setMaxInactiveInterval(60 * 60);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Administrator logged in successfully.",
                        "username", admin.getUsername(),
                        "isAdmin", true
                )
        );
    }

    // Verify the current administrator session.
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return unauthorized();
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            return unauthorized();
        }

        Optional<User> existingUser =
                userRepository.findById((Long) userId);

        if (existingUser.isEmpty()
                || !existingUser.get().isAdmin()) {
            return unauthorized();
        }

        User admin = existingUser.get();

        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true,
                        "isAdmin", true,
                        "userId", admin.getId(),
                        "username", admin.getUsername()
                )
        );
    }

    // End the current session.
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Logged out successfully."
                )
        );
    }

    private ResponseEntity<Map<String, Object>> invalidCredentials() {
        return ResponseEntity.status(401).body(
                Map.of(
                        "success", false,
                        "message", "Invalid administrator credentials."
                )
        );
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        return ResponseEntity.status(401).body(
                Map.of(
                        "authenticated", false,
                        "isAdmin", false,
                        "message", "Administrator authentication required."
                )
        );
    }
}
