package com.training.ecommerce.dto;

/** A coupon as exposed by the REST API. */
public record CouponResponse(String code, String description, boolean active) {
}
