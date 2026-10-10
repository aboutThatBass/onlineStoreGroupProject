
package com.codegoats.viralvault.user;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserValidationService {

    public List<String> validateRegistration(RegistrationRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Registration information is required.");
            return errors;
        }

        if (isBlank(request.getUsername())) {
            errors.add("Username is required.");
        } else if (request.getUsername().length() > 50) {
            errors.add("Username must be 50 characters or fewer.");
        }

        if (isBlank(request.getEmail())) {
            errors.add("Email is required.");
        } else if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.add("Please enter a valid email address.");
        }

        if (isBlank(request.getPhoneNumber())) {
            errors.add("Phone number is required.");
        } else if (!request.getPhoneNumber().matches("^[0-9()+.\\s-]{7,20}$")) {
            errors.add("Please enter a valid phone number.");
        }

        if (isBlank(request.getPassword())) {
            errors.add("Password is required.");
        } else if (request.getPassword().length() < 8) {
            errors.add("Password must be at least 8 characters.");
        }

        if (isBlank(request.getConfirmPassword())) {
            errors.add("Password confirmation is required.");
        } else if (!request.getConfirmPassword().equals(request.getPassword())) {
            errors.add("Passwords do not match.");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
