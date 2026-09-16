package com.training.ecommerce.web;

import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.dto.CreateCouponRequest;
import com.training.ecommerce.dto.CouponResponse;
import com.training.ecommerce.service.CouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-style endpoints for managing coupon campaigns. */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(@RequestBody CreateCouponRequest request) {
        Coupon created = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public List<CouponResponse> list() {
        return couponService.list().stream().map(CouponController::toResponse).toList();
    }

    @GetMapping("/{code}")
    public CouponResponse getOne(@PathVariable String code) {
        return toResponse(couponService.getByCode(code));
    }

    /** Body-less toggle: /api/coupons/SAVE10/active?active=false */
    @PatchMapping("/{code}/active")
    public CouponResponse setActive(@PathVariable String code,
                                    @RequestParam(defaultValue = "true") boolean active) {
        return toResponse(couponService.setActive(code, active));
    }

    static CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(coupon.getCode(), coupon.describe(), coupon.isActive());
    }
}
