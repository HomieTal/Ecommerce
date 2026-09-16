package com.training.ecommerce.domain.discount;

import java.math.BigDecimal;

/**
 * The Strategy pattern in its purest form: one interface, interchangeable
 * implementations, selected at runtime.
 *
 * <p>A strategy answers a single question: "for this cart, how much money
 * should be taken off the subtotal?". Implementations must never return a
 * negative amount; the pricing service additionally caps the returned discount
 * at the subtotal, so a coupon can never make the shop pay the customer.</p>
 */
public interface DiscountStrategy {

    /**
     * @param context the cart subtotal and its lines
     * @return the discount amount (zero or positive, 2 decimal places)
     */
    BigDecimal computeDiscount(DiscountContext context);

    /** Human-readable description, e.g. {@code "10% off the order subtotal"}. */
    String describe();
}
