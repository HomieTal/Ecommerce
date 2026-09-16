package com.training.ecommerce.dto;

import com.training.ecommerce.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** A placed order as exposed by the REST API. */
public record OrderResponse(String id, String customerId, List<OrderLineResponse> items,
                            BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                            String couponCode, OrderStatus status, Instant createdAt) {
}
