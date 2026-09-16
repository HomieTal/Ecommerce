package com.training.ecommerce.domain.model;

import java.math.BigDecimal;

/**
 * Value object holding the priced result of a cart:
 * subtotal before discounts, discount granted, and the final total.
 */
public record PriceSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                           String couponCode, String couponDescription) {

    public PriceSummary {
        if (subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("Subtotal must not be negative");
        }
        if (discount == null || discount.signum() < 0) {
            throw new IllegalArgumentException("Discount must not be negative");
        }
        if (total == null || total.signum() < 0) {
            throw new IllegalArgumentException("Total must not be negative");
        }
    }

    public static PriceSummary withoutCoupon(BigDecimal subtotal) {
        return new PriceSummary(subtotal, BigDecimal.ZERO.setScale(2), subtotal, null, null);
    }
}
