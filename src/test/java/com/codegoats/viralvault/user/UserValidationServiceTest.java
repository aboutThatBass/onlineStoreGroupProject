
package com.codegoats.viralvault.user;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationServiceTest {

    private final UserValidationService validationService = new UserValidationService();

    private RegistrationRequest validRequest() {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("viralshopper21");
        request.setFullName("Alex Smith");
        request.setEmail("alex@example.com");
        request.setPhoneNumber("2105551234");
        request.setPassword("ExamplePassword123!");
        request.setConfirmPassword("ExamplePassword123!");
        return request;
    }

    @Test
    void validRegistrationHasNoErrors() {
        List<String> errors = validationService.validateRegistration(validRequest());
        assertTrue(errors.isEmpty());
    }

    @Test
    void missingUsernameIsRejected() {
        RegistrationRequest request = validRequest();
        request.setUsername("");

        List<String> errors = validationService.validateRegistration(request);
        assertTrue(errors.contains("Username is required."));
    }

    @Test
    void invalidEmailIsRejected() {
        RegistrationRequest request = validRequest();
        request.setEmail("invalidemail");

        List<String> errors = validationService.validateRegistration(request);
        assertTrue(errors.contains("Please enter a valid email address."));
    }

    @Test
    void missingPhoneNumberIsRejected() {
        RegistrationRequest request = validRequest();
        request.setPhoneNumber("");

        List<String> errors = validationService.validateRegistration(request);
        assertTrue(errors.contains("Phone number is required."));
    }

    @Test
    void shortPasswordIsRejected() {
        RegistrationRequest request = validRequest();
        request.setPassword("abc");
        request.setConfirmPassword("abc");

        List<String> errors = validationService.validateRegistration(request);
        assertTrue(errors.contains("Password must be at least 8 characters."));
    }

    @Test
    void mismatchedPasswordsAreRejected() {
        RegistrationRequest request = validRequest();
        request.setConfirmPassword("DifferentPassword123!");

        List<String> errors = validationService.validateRegistration(request);
        assertTrue(errors.contains("Passwords do not match."));
    }

    @Test
    void missingRegistrationRequestIsRejected() {
        List<String> errors = validationService.validateRegistration(null);
        assertTrue(errors.contains("Registration information is required."));
    }
}
