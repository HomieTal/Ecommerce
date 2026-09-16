package com.training.ecommerce.dto;

/** Request body for moving an order to a new lifecycle status. */
public record StatusUpdateRequest(String status) {
}
