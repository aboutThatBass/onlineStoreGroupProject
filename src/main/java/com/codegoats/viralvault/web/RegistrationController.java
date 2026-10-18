
package com.codegoats.viralvault.web;

import java.util.List;
import java.util.Map;

import com.codegoats.viralvault.user.RegistrationRequest;
import com.codegoats.viralvault.user.UserRegistrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class RegistrationController {

    private final UserRegistrationService registrationService;

    public RegistrationController(
            UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegistrationRequest request) {

        List<String> errors = registrationService.registerUser(request);

        if (!errors.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "message", "Account created successfully!"
                ));
    }
}
