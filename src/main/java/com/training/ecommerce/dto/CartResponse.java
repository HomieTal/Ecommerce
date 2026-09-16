package com.training.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

/** The customer's cart, fully priced: subtotal, discount and total. */
public record CartResponse(String customerId, List<CartLineResponse> items, int totalQuantity,
                           String couponCode, String couponDescription,
                           BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
}
