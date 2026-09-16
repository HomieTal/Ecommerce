package com.training.ecommerce.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Spend-more-save-more: the cart subtotal picks the highest bracket whose
 * threshold it reaches, and that bracket's percentage is applied.
 *
 * <p>Example tiers: spend &ge; 1000 &rarr; 5% off, spend &ge; 2000 &rarr; 10% off.
 * A subtotal of 2500 matches both and wins the 10% bracket.</p>
 */
public class TieredDiscountStrategy implements DiscountStrategy {

    /** One pricing bracket: minimum subtotal and the percentage off it unlocks. */
    public record Tier(BigDecimal minSubtotal, BigDecimal percentOff) {
        public Tier {
            if (minSubtotal == null || minSubtotal.signum() < 0) {
                throw new IllegalArgumentException("Tier threshold must not be negative");
            }
            if (percentOff == null || percentOff.signum() <= 0
                    || percentOff.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Tier percent must be between 0 (exclusive) and 100 (inclusive)");
            }
        }
    }

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final List<Tier> tiers; // sorted ascending by threshold

    public TieredDiscountStrategy(List<Tier> tiers) {
        Objects.requireNonNull(tiers, "Tiers are required");
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("At least one tier is required");
        }
        this.tiers = tiers.stream().sorted(Comparator.comparing(Tier::minSubtotal)).toList();
    }

    @Override
    public BigDecimal computeDiscount(DiscountContext context) {
        return tiers.stream()
                .filter(tier -> context.subtotal().compareTo(tier.minSubtotal()) >= 0)
                .max(Comparator.comparing(Tier::minSubtotal))
                .map(tier -> context.subtotal()
                        .multiply(tier.percentOff())
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public String describe() {
        String parts = tiers.stream()
                .map(tier -> "spend >= %s -> %s%% off"
                        .formatted(tier.minSubtotal().toPlainString(),
                                tier.percentOff().stripTrailingZeros().toPlainString()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "Tiered discount [" + parts + "]";
    }
}
