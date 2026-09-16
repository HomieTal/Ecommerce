package com.training.ecommerce.dto;

import com.training.ecommerce.domain.discount.DiscountType;
import com.training.ecommerce.domain.discount.TieredDiscountStrategy;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for creating a coupon. Which fields are used depends on the
 * {@link DiscountType}:
 * <ul>
 *   <li>PERCENTAGE / FLAT_AMOUNT - {@code value}</li>
 *   <li>BUY_X_GET_Y_FREE - {@code targetProductId}, {@code buyX}, {@code freeY}</li>
 *   <li>TIERED - {@code tiers}</li>
 * </ul>
 */
public record CreateCouponRequest(String code, DiscountType type, BigDecimal value,
                                  String targetProductId, Integer buyX, Integer freeY,
                                  List<TieredDiscountStrategy.Tier> tiers) {
}
