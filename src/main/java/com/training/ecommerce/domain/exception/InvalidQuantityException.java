package com.training.ecommerce.domain.exception;

/** Thrown when a quantity of 0 or less is used for cart operations. */
public class InvalidQuantityException extends ECommerceException {

    public InvalidQuantityException(int quantity) {
        super("Quantity must be at least 1, got %d".formatted(quantity));
    }
}
