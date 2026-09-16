package com.training.ecommerce.domain.model;

import com.training.ecommerce.domain.discount.DiscountStrategy;

import java.util.Locale;
import java.util.Objects;

/**
 * A coupon ties a human-friendly code to a {@link DiscountStrategy}.
 *
 * <p>This is the bridge between "marketing" (a code a customer can type) and
 * "pricing rules" (the strategy that knows how to compute the discount).
 * Adding a new kind of campaign never changes this class - you only write a
 * new strategy implementation.</p>
 */
public class Coupon {

    private final String code;
    private final DiscountStrategy strategy;
    private boolean active = true;

    public Coupon(String code, DiscountStrategy strategy) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Coupon code must not be blank");
        }
        Objects.requireNonNull(strategy, "Discount strategy is required");
        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.strategy = strategy;
    }

    public String getCode() {
        return code;
    }

    public DiscountStrategy getStrategy() {
        return strategy;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public String describe() {
        return strategy.describe();
    }

    @Override
    public String toString() {
        return "%s - %s%s".formatted(code, describe(), active ? "" : " [INACTIVE]");
    }
}
