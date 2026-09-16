package com.training.ecommerce.dto;

import java.math.BigDecimal;

/** Request body for updating a product price. */
public record UpdatePriceRequest(BigDecimal price) {
}
