package com.training.ecommerce.domain.exception;

/**
 * Thrown when a quantity is requested that the warehouse cannot fulfil, or a
 * cart update would exceed the available stock.
 */
public class InsufficientStockException extends ECommerceException {

    private final String productName;
    private final int requested;
    private final int available;

    public InsufficientStockException(String productName, int requested, int available) {
        super("Insufficient stock for '%s': requested %d, available %d"
                .formatted(productName, requested, available));
        this.productName = productName;
        this.requested = requested;
        this.available = available;
    }

    public String getProductName() {
        return productName;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
