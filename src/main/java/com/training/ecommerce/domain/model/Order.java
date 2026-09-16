package com.training.ecommerce.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A placed order: immutable money figures, immutable item snapshots and a
 * mutable status that may only change through the enum's transition rules.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;
    private final String couponCode;   // nullable
    private final Instant createdAt;
    private OrderStatus status;

    public Order(String id, String customerId, List<OrderItem> items,
                 BigDecimal subtotal, BigDecimal discount, String couponCode, Instant createdAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Order customer id must not be blank");
        }
        Objects.requireNonNull(items, "Order items are required");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
        if (subtotal == null || subtotal.signum() < 0) {
            throw new IllegalArgumentException("Order subtotal must not be negative");
        }
        if (discount == null || discount.signum() < 0) {
            throw new IllegalArgumentException("Order discount must not be negative");
        }
        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Order discount cannot exceed the subtotal");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Order creation time is required");
        }
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.subtotal = subtotal.setScale(2, java.math.RoundingMode.HALF_UP);
        this.discount = discount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.total = this.subtotal.subtract(this.discount);
        this.couponCode = (couponCode == null || couponCode.isBlank()) ? null : couponCode;
        this.createdAt = createdAt;
        this.status = OrderStatus.CONFIRMED; // a new order starts confirmed (payment simulated)
    }

    /** Applies a lifecycle transition, enforcing the rules encoded in {@link OrderStatus}. */
    public void transitionTo(OrderStatus target) {
        Objects.requireNonNull(target, "Target status is required");
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Illegal status transition %s -> %s for order %s".formatted(status, target, id));
        }
        this.status = target;
    }

    /** Cancels the order if it is still cancellable. */
    public void cancel() {
        if (!status.isCancellable()) {
            throw new IllegalStateException(
                    "Order %s in status %s can no longer be cancelled".formatted(id, status));
        }
        this.status = OrderStatus.CANCELLED;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
