
package com.codegoats.viralvault.web;

import com.codegoats.viralvault.user.User;
import com.codegoats.viralvault.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/profile")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record ProfileRequest(
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String city,
        String state,
        String zipCode
    ) {}

    public record ProfileResponse(
        String username,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String city,
        String state,
        String zipCode
    ) {}

    private Long getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            return null;
        }

        return (Long) userId;
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getCity(),
            user.getState(),
            user.getZipCode()
        );
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    @GetMapping
    public ResponseEntity<?> getProfile(
            HttpServletRequest request) {

        Long userId = getLoggedInUserId(request);

        if (userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "Please log in."));
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "Account not found."));
        }

        return ResponseEntity.ok(toResponse(user.get()));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileRequest body,
            HttpServletRequest request) {

        Long userId = getLoggedInUserId(request);

        if (userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "Please log in."));
        }

        Optional<User> existing = userRepository.findById(userId);

        if (existing.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("message", "Account not found."));
        }

        if (body == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Profile information is required."));
        }

        String fullName = clean(body.fullName());
        String email = clean(body.email()).toLowerCase(Locale.ROOT);
        String phone = clean(body.phoneNumber());
        String address = clean(body.address());
        String city = clean(body.city());
        String state = clean(body.state());
        String zipCode = clean(body.zipCode());

        if (fullName.length() > 100) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Full name is too long."));
        }

        if (email.length() > 100 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Please enter a valid email address."));
        }

        if (!phone.matches("^[0-9()+.\\s-]{7,20}$")) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Please enter a valid phone number."));
        }

        if (address.length() > 255 ||
                city.length() > 100 ||
                state.length() > 50 ||
                zipCode.length() > 20) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "One or more address fields are too long."));
        }

        if (userRepository.emailUsedByAnotherUser(email, userId)) {
            return ResponseEntity.status(409)
                .body(Map.of("message", "That email is already in use."));
        }

        User user = existing.get();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setAddress(address);
        user.setCity(city);
        user.setState(state);
        user.setZipCode(zipCode);

        try {
            int updated = userRepository.updateProfile(user);

            if (updated != 1) {
                return ResponseEntity.status(404)
                    .body(Map.of("message", "Account not found."));
            }
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409)
                .body(Map.of("message", "That email is already in use."));
        }

        return ResponseEntity.ok(toResponse(user));
    }
}
