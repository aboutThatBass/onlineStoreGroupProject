package com.codegoats.viralvault.discount;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Discount getDiscountById(Long id) {
        return discountRepository.findById(id);
    }

    private String normalizeAndValidateCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("Discount code is required.");
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("Discount code cannot exceed 50 characters.");
        }
        return normalized;
    }

    private double validatePercent(Double rawPercent) {
        if (rawPercent == null || !Double.isFinite(rawPercent) || rawPercent <= 0 || rawPercent > 100) {
            throw new IllegalArgumentException("Discount percentage must be greater than 0 and at most 100.");
        }
        return Math.round(rawPercent * 100.0) / 100.0;
    }

    public Discount createDiscount(String code, Double discountPercent, Boolean isActive) {
        String cleanCode = normalizeAndValidateCode(code);
        double cleanPercent = validatePercent(discountPercent);
        boolean active = (isActive == null) ? true : isActive;

        Discount discount = new Discount(null, cleanCode, cleanPercent, active);
        discountRepository.addDiscount(discount);
        return discountRepository.findByCode(cleanCode);
    }

    public Discount patchDiscount(Long id, String code, Double discountPercent, Boolean isActive) {
        Discount existing = discountRepository.findById(id);
        if (existing == null) {
            return null;
        }

        String updatedCode = (code != null)
            ? normalizeAndValidateCode(code)
            : existing.getCode();

        double updatedPercent = (discountPercent != null)
            ? validatePercent(discountPercent)
            : existing.getDiscountPercent();

        boolean updatedActive = (isActive != null)
            ? isActive
            : existing.isActive();

        Discount updated = new Discount(id, updatedCode, updatedPercent, updatedActive);
        boolean saved = discountRepository.updateDiscount(id, updated);
        return saved ? discountRepository.findById(id) : null;
    }
}