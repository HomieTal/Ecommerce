package com.training.ecommerce.domain.exception;

/** Thrown when an operation references a product id that is not in the catalogue. */
public class ProductNotFoundException extends ECommerceException {

    public ProductNotFoundException(String productId) {
        super("Product '%s' was not found in the catalogue".formatted(productId));
    }
}
