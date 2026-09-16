package com.training.ecommerce.domain.exception;

/** Thrown when checkout is attempted with an empty (or missing) cart. */
public class EmptyCartException extends ECommerceException {

    public EmptyCartException(String customerId) {
        super("Cart of customer '%s' is empty - add products before checkout".formatted(customerId));
    }
}
