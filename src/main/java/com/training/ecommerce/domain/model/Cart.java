package com.training.ecommerce.domain.model;

import com.training.ecommerce.domain.exception.ProductNotInCartException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A customer's shopping cart.
 *
 * <p>This is an example of a domain object that carries behaviour (not just
 * data): adding, merging, updating and removing lines are cart business rules,
 * so they live here instead of inside a service. Lines keep insertion order,
 * which makes the cart display stable.</p>
 */
public class Cart {

    private final String customerId;
    private final Map<String, CartItem> items = new LinkedHashMap<>();
    private String couponCode;

    public Cart(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be blank");
        }
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    /** Adds units of a product; repeated adds for the same product merge into one line. */
    public void addItem(String productId, int quantity) {
        CartItem existing = items.get(productId);
        if (existing == null) {
            items.put(productId, new CartItem(productId, quantity));
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
        }
    }

    public void updateQuantity(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new ProductNotInCartException(productId);
        }
        item.setQuantity(quantity);
    }

    public void removeItem(String productId) {
        if (items.remove(productId) == null) {
            throw new ProductNotInCartException(productId);
        }
    }

    public Optional<CartItem> findItem(String productId) {
        return Optional.ofNullable(items.get(productId));
    }

    /** Unmodifiable, insertion-ordered view of the cart lines. */
    public List<CartItem> getItems() {
        return List.copyOf(items.values());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int totalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    /** Empties the cart and removes any applied coupon. */
    public void clear() {
        items.clear();
        this.couponCode = null;
    }
}
