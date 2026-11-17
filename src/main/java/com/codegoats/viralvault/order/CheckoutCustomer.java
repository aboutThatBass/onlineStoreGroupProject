package com.codegoats.viralvault.order;

/** Shipping/contact details supplied at checkout, independent of the account profile. */
public record CheckoutCustomer(String firstName, String lastName, String email,
        String address, String city, String state, String zipCode) {
    private static String required(String value, String label, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(label + " is required and must be at most " + max + " characters.");
        }
        return value.trim();
    }

    public CheckoutCustomer validated() {
        String normalizedEmail = required(email, "Email", 100);
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Please enter a valid checkout email address.");
        }
        return new CheckoutCustomer(required(firstName, "First name", 100),
                required(lastName, "Last name", 100), normalizedEmail,
                required(address, "Street address", 255), required(city, "City", 100),
                required(state, "State", 50), required(zipCode, "ZIP code", 20));
    }
}
