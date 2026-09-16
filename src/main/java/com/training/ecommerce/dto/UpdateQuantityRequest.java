package com.training.ecommerce.dto;

/** Request body for setting a cart line to an absolute quantity. */
public record UpdateQuantityRequest(int quantity) {
}
