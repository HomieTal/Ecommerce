package com.training.ecommerce.dto;

import com.training.ecommerce.domain.model.ProductCategory;

import java.math.BigDecimal;

/** Request body for creating a catalogue product. */
public record CreateProductRequest(String name, String description, ProductCategory category,
                                   BigDecimal price, int initialStock) {
}
