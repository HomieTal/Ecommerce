package com.training.ecommerce.dto;

import com.training.ecommerce.domain.model.ProductCategory;

import java.math.BigDecimal;

/** One line of a placed order - the immutable purchase snapshot. */
public record OrderLineResponse(String productId, String name, ProductCategory category,
                                BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
