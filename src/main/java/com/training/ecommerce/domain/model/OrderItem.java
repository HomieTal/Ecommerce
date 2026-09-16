package com.training.ecommerce.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An immutable line of a placed order - a snapshot of what was bought.
 *
 * <p>Unlike a {@link CartItem}, an order line copies the product name,
 * category and unit price. Orders are historical documents: later catalogue
 * edits or price changes must never rewrite the past. Records (Java 17) are a
 * perfect fit for this kind of immutable value.</p>
 */
public record OrderItem(String productId, String productName, ProductCategory category,
                        BigDecimal unitPrice, int quantity) {

    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Order item product id must not be blank");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Order item product name must not be blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("Order item category is required");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Order item unit price must be greater than zero");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Order item quantity must be at least 1, got " + quantity);
        }
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
