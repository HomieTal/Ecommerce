package com.training.ecommerce.repository;

import com.training.ecommerce.domain.model.Coupon;

import java.util.List;
import java.util.Optional;

/** Persistence contract for coupons, keyed by their uppercase code. */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    List<Coupon> findAll();
}
