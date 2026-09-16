package com.training.ecommerce.domain.discount;

/**
 * The kinds of discount strategies the shop ships with. The coupon service
 * maps each value to a concrete {@link DiscountStrategy} via a switch
 * expression - adding a new campaign type means adding one enum constant,
 * one strategy class and one switch branch.
 */
public enum DiscountType {
    PERCENTAGE,
    FLAT_AMOUNT,
    BUY_X_GET_Y_FREE,
    TIERED
}
