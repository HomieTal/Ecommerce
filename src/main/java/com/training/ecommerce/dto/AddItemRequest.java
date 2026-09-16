package com.training.ecommerce.dto;

/** Request body for adding a product to a cart. */
public record AddItemRequest(String productId, int quantity) {
}
