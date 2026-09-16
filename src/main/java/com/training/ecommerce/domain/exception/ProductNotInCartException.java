package com.training.ecommerce.domain.exception;

/** Thrown when updating/removing a cart line that the cart does not contain. */
public class ProductNotInCartException extends ECommerceException {

    public ProductNotInCartException(String productId) {
        super("Product '%s' is not in the cart".formatted(productId));
    }
}
