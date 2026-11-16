package com.codegoats.viralvault.discount;

public class Discount {

    private Long id;
    private String code;
    private double discountPercent;
    private boolean active;

    public Discount(Long id, String code, double discountPercent, boolean active) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}