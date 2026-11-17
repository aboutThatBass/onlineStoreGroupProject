package com.codegoats.viralvault.discount;

public class Discount {

    private Long id;
    private String code;
    private double discountPercent;
    private String discountType;
    private double fixedAmount;
    private boolean active;

    public Discount(
            Long id,
            String code,
            double discountPercent,
            boolean active) {

        this(
            id,
            code,
            discountPercent,
            "percent",
            0,
            active
        );
    }

    public Discount(
            Long id,
            String code,
            double discountPercent,
            String discountType,
            double fixedAmount,
            boolean active) {

        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.discountType = discountType;
        this.fixedAmount = fixedAmount;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(double fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}