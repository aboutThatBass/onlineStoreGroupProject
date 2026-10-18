
package com.codegoats.viralvault.web;

import java.util.Map;

import com.codegoats.viralvault.user.UserLoginService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class LoginController {

    private final UserLoginService loginService;

    public LoginController(UserLoginService loginService) {
        this.loginService = loginService;
    }

    // Represents the information submitted by the login form
    public record LoginRequest(String email, String password) {
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request) {

        boolean success = loginService.login(
                request.email(),
                request.password()
        );

        if (!success) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid email or password."
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Login credentials verified successfully!"
                )
        );
    }
}
