
package com.codegoats.viralvault.web;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserLoginService;
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
@RequestMapping("/api/users")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class LoginController {

    private final UserLoginService loginService;
    private final UserRepository userRepository;

    public LoginController(
            UserLoginService loginService,
            UserRepository userRepository) {
        this.loginService = loginService;
        this.userRepository = userRepository;
    }

    public record LoginRequest(String email, String password) {
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        boolean success = loginService.login(
                request.email(),
                request.password()
        );

        if (!success) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "success", false,
                            "message", "Invalid email or password."
                    )
            );
        }

        String normalizedEmail =
                request.email().trim().toLowerCase(Locale.ROOT);

        Optional<User> user = userRepository.findByEmail(normalizedEmail);

        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "success", false,
                            "message", "Unable to find user account."
                    )
            );
        }

        // Replace any existing session to prevent session fixation.
        HttpSession previousSession = httpRequest.getSession(false);

        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session = httpRequest.getSession(true);

        // Store only the user ID, never the password.
        session.setAttribute("userId", user.get().getId());
        session.setMaxInactiveInterval(60 * 60);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Logged in successfully!",
                        "username", user.get().getUsername()
                )
        );
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "authenticated", false,
                            "message", "Not logged in."
                    )
            );
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            return ResponseEntity.status(401).body(
                    Map.of(
                            "authenticated", false,
                            "message", "Not logged in."
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true,
                        "userId", userId
                )
        );
    }

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
}
