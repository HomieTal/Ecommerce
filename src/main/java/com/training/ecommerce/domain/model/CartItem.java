package com.training.ecommerce.domain.model;

import com.training.ecommerce.domain.exception.InvalidQuantityException;

/**
 * A single line of a shopping cart: which product and how many units.
 *
 * <p>The cart line stores only the product id and the quantity - the price is
 * always looked up from the live catalogue when totals are computed, so a
 * price change is reflected immediately while shopping.</p>
 */
public class CartItem {

    private final String productId;
    private int quantity;

    public CartItem(String productId, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Cart item product id must not be blank");
        }
        if (quantity < 1) {
            throw new InvalidQuantityException(quantity);
        }
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    /** Business rule: a cart line must always hold at least one unit. */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidQuantityException(quantity);
        }
        this.quantity = quantity;
    }
}
