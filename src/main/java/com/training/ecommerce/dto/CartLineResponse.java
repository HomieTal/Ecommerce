package com.training.ecommerce.dto;

import com.training.ecommerce.domain.model.ProductCategory;

import java.math.BigDecimal;

/** One priced cart line as shown to the customer. */
public record CartLineResponse(String productId, String name, ProductCategory category,
                               BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
