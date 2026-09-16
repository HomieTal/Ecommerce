package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.BuyXGetFreeStrategy;
import com.training.ecommerce.domain.discount.DiscountStrategy;
import com.training.ecommerce.domain.discount.DiscountType;
import com.training.ecommerce.domain.discount.FlatAmountDiscountStrategy;
import com.training.ecommerce.domain.discount.PercentageDiscountStrategy;
import com.training.ecommerce.domain.discount.TieredDiscountStrategy;
import com.training.ecommerce.domain.exception.CouponNotFoundException;
import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.dto.CreateCouponRequest;
import com.training.ecommerce.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Manages coupon definitions. The enum-to-strategy mapping below is a compact
 * demonstration of Java 17 switch expressions and of the Strategy pattern:
 * the service never multiplies or subtracts money itself - it only wires
 * rules together.
 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon create(CreateCouponRequest request) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            throw new IllegalArgumentException("Coupon code must not be blank");
        }
        String normalized = request.code().trim().toUpperCase(Locale.ROOT);
        couponRepository.findByCode(normalized).ifPresent(existing -> {
            throw new IllegalStateException("Coupon '%s' already exists".formatted(normalized));
        });

        Coupon coupon = new Coupon(normalized, buildStrategy(request, normalized));
        return couponRepository.save(coupon);
    }

    public List<Coupon> list() {
        return couponRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Coupon::getCode))
                .toList();
    }

    public Coupon getByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException(code));
    }

    public Coupon setActive(String code, boolean active) {
        Coupon coupon = getByCode(code);
        if (active) {
            coupon.activate();
        } else {
            coupon.deactivate();
        }
        return couponRepository.save(coupon);
    }

    private DiscountStrategy buildStrategy(CreateCouponRequest request, String normalizedCode) {
        if (request.type() == null) {
            throw new IllegalArgumentException("Discount type is required for coupon '%s'".formatted(normalizedCode));
        }
        return switch (request.type()) {
            case PERCENTAGE -> new PercentageDiscountStrategy(requireValue(request));
            case FLAT_AMOUNT -> new FlatAmountDiscountStrategy(requireValue(request));
            case BUY_X_GET_Y_FREE -> new BuyXGetFreeStrategy(
                    requireField(request.targetProductId(), "targetProductId"),
                    requireInt(request.buyX(), "buyX"),
                    requireInt(request.freeY(), "freeY"));
            case TIERED -> new TieredDiscountStrategy(
                    java.util.Objects.requireNonNull(request.tiers(), "tiers are required for TIERED coupons"));
        };
    }

    private java.math.BigDecimal requireValue(CreateCouponRequest request) {
        return java.util.Objects.requireNonNull(request.value(),
                "value is required for %s coupons".formatted(request.type()));
    }

    private String requireField(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private int requireInt(Integer value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
