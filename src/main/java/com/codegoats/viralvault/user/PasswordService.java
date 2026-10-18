
package com.codegoats.viralvault.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Hash a password before storing it in the database
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    // Check whether a password matches its stored hash
    public boolean verifyPassword(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }
}
