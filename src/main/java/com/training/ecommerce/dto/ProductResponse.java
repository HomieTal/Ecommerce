package com.training.ecommerce.dto;

import com.training.ecommerce.domain.model.ProductCategory;

import java.math.BigDecimal;

/** A catalogue product as exposed by the REST API, including live stock. */
public record ProductResponse(String id, String name, String description, ProductCategory category,
                              BigDecimal price, int stock) {
}
