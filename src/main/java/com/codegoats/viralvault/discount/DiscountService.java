package com.codegoats.viralvault.discount;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(
            DiscountRepository discountRepository) {

        this.discountRepository = discountRepository;
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Discount getDiscountById(Long id) {
        return discountRepository.findById(id);
    }

    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                "Discount code is required."
            );
        }

        String normalized =
            code.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() > 50) {
            throw new IllegalArgumentException(
                "Discount code cannot exceed 50 characters."
            );
        }

        return normalized;
    }

    private String validateType(String type) {

        if (type == null) {
            return "percent";
        }

        String normalized =
            type.trim().toLowerCase(Locale.ROOT);

        if (!normalized.equals("percent")
                && !normalized.equals("fixed")) {

            throw new IllegalArgumentException(
                "Invalid discount type."
            );
        }

        return normalized;
    }

    private double validateValue(
            Double value,
            double maximum) {

        if (value == null
                || !Double.isFinite(value)
                || value < 0.01
                || value > maximum) {

            throw new IllegalArgumentException(
                "Invalid discount amount."
            );
        }

        return Math.round(value * 100.0) / 100.0;
    }

    private Discount buildDiscount(
            Long id,
            String code,
            String type,
            Double percent,
            Double fixedAmount,
            boolean active) {

        String normalizedCode = normalizeCode(code);
        String normalizedType = validateType(type);

        double percentage = 0;
        double fixed = 0;

        if (normalizedType.equals("percent")) {
            percentage = validateValue(percent, 100);
        } else {
            fixed = validateValue(fixedAmount, 99999999.99);
        }

        return new Discount(
            id,
            normalizedCode,
            percentage,
            normalizedType,
            fixed,
            active
        );
    }

    public Discount createDiscount(
            String code,
            Double percent,
            Boolean active) {

        return createDiscount(
            code, percent, active, "percent", null
        );
    }

    public Discount createDiscount(
            String code,
            Double percent,
            Boolean active,
            String type,
            Double fixedAmount) {

        Discount discount = buildDiscount(
            null,
            code,
            type,
            percent,
            fixedAmount,
            active == null || active
        );

        discountRepository.addDiscount(discount);

        return discountRepository.findByCode(
            discount.getCode()
        );
    }

    public Discount patchDiscount(
            Long id,
            String code,
            Double percent,
            Boolean active) {

        return patchDiscount(
            id, code, percent, active, null, null
        );
    }

    public Discount patchDiscount(
            Long id,
            String code,
            Double percent,
            Boolean active,
            String type,
            Double fixedAmount) {

        Discount existing =
            discountRepository.findById(id);

        if (existing == null) {
            return null;
        }

        Discount updated = buildDiscount(
            id,
            code == null ? existing.getCode() : code,
            type == null ? existing.getDiscountType() : type,
            percent == null
                ? existing.getDiscountPercent()
                : percent,
            fixedAmount == null
                ? existing.getFixedAmount()
                : fixedAmount,
            active == null ? existing.isActive() : active
        );

        if (!discountRepository.updateDiscount(id, updated)) {
            return null;
        }

        return discountRepository.findById(id);
    }

    public Discount validateDiscount(String code) {

        Discount discount = discountRepository.findByCode(
            normalizeCode(code)
        );

        if (discount == null || !discount.isActive()) {
            throw new IllegalArgumentException(
                "Invalid or inactive discount code."
            );
        }

        return discount;
    }

    public BigDecimal calculateDiscount(
            String code,
            BigDecimal subtotal) {

        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO;
        }

        Discount discount = validateDiscount(code);

        BigDecimal amount;

        if ("fixed".equals(discount.getDiscountType())) {

            amount = BigDecimal.valueOf(
                discount.getFixedAmount()
            );

        } else {

            amount = subtotal.multiply(
                BigDecimal.valueOf(
                    discount.getDiscountPercent()
                ).movePointLeft(2)
            );
        }

        return amount
            .min(subtotal)
            .setScale(2, RoundingMode.HALF_UP);
    }
}