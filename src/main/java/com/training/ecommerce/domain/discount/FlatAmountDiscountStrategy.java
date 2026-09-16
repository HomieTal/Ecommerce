package com.training.ecommerce.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Takes a fixed amount off the subtotal. The pricing service caps the result
 * at the subtotal, so this strategy is safe even when the coupon amount is
 * larger than the cart value.
 */
public class FlatAmountDiscountStrategy implements DiscountStrategy {

    private final BigDecimal amount;

    public FlatAmountDiscountStrategy(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Discount amount must be greater than zero");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal computeDiscount(DiscountContext context) {
        return amount.min(context.subtotal());
    }

    @Override
    public String describe() {
        return "Flat " + amount.toPlainString() + " off the order subtotal";
    }
}
