package com.training.ecommerce.domain.exception;

/** Thrown when an operation references an order id that does not exist. */
public class OrderNotFoundException extends ECommerceException {

    public OrderNotFoundException(String orderId) {
        super("Order '%s' was not found".formatted(orderId));
    }
}
