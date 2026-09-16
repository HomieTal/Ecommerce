package com.training.ecommerce.domain.exception;

/** Thrown when an unknown coupon code is applied or priced. */
public class CouponNotFoundException extends ECommerceException {

    public CouponNotFoundException(String code) {
        super("Coupon '%s' does not exist".formatted(code));
    }
}
