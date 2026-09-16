package com.training.ecommerce.domain.discount;

import com.training.ecommerce.domain.model.ProductCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Input passed to every {@link DiscountStrategy}: the cart subtotal plus the
 * individual lines, so both subtotal-based and line-based campaigns can be
 * implemented against the same contract.
 */
public record DiscountContext(BigDecimal subtotal, List<Line> lines) {

    public DiscountContext {
        if (subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("Discount context subtotal must not be negative");
        }
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    /** A snapshot of one cart line relevant for pricing decisions. */
    public record Line(String productId, String productName, ProductCategory category,
                       BigDecimal unitPrice, int quantity) {

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }
}
