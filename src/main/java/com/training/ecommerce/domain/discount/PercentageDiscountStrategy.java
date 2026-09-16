package com.training.ecommerce.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Takes a fixed percentage off the whole subtotal, e.g. {@code SAVE10 -> 10% off}. */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final BigDecimal percent;

    public PercentageDiscountStrategy(BigDecimal percent) {
        Objects.requireNonNull(percent, "Percent is required");
        if (percent.signum() <= 0 || percent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Percent must be between 0 (exclusive) and 100 (inclusive)");
        }
        this.percent = percent;
    }

    @Override
    public BigDecimal computeDiscount(DiscountContext context) {
        return context.subtotal()
                .multiply(percent)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return percent.stripTrailingZeros().toPlainString() + "% off the order subtotal";
    }
}
