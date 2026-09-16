package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.BuyXGetFreeStrategy;
import com.training.ecommerce.domain.discount.DiscountContext;
import com.training.ecommerce.domain.discount.DiscountStrategy;
import com.training.ecommerce.domain.exception.CouponNotFoundException;
import com.training.ecommerce.domain.model.Cart;
import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.domain.model.PriceSummary;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.repository.CouponRepository;
import com.training.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Single place where cart money maths happens, shared by "view cart" and
 * "checkout" so the numbers a customer sees are exactly the numbers they pay.
 */
@Service
public class PricingService {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public PricingService(ProductRepository productRepository, CouponRepository couponRepository) {
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    /** Resolves cart lines against the live catalogue (name/category/price snapshots). */
    public List<DiscountContext.Line> buildLines(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new com.training.ecommerce.domain.exception.ProductNotFoundException(
                                    item.getProductId()));
                    return new DiscountContext.Line(
                            product.getId(),
                            product.getName(),
                            product.getCategory(),
                            product.getPrice(),
                            item.getQuantity());
                })
                .toList();
    }

    public PriceSummary summarize(Cart cart) {
        List<DiscountContext.Line> lines = buildLines(cart);

        BigDecimal subtotal = lines.stream()
                .map(DiscountContext.Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (cart.getCouponCode() == null) {
            return PriceSummary.withoutCoupon(subtotal);
        }

        Coupon coupon = couponRepository.findByCode(cart.getCouponCode())
                .orElseThrow(() -> new CouponNotFoundException(cart.getCouponCode()));
        if (!coupon.isActive()) {
            throw new IllegalStateException("Coupon '%s' is no longer active".formatted(coupon.getCode()));
        }

        DiscountStrategy strategy = coupon.getStrategy();
        BigDecimal discount = strategy.computeDiscount(new DiscountContext(subtotal, lines))
                .min(subtotal) // business rule: a discount can never exceed what is being bought
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount);

        return new PriceSummary(subtotal, discount, total, coupon.getCode(), coupon.describe());
    }
}
