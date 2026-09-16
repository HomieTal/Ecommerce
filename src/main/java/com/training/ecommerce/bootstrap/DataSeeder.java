package com.training.ecommerce.bootstrap;

import com.training.ecommerce.domain.discount.DiscountType;
import com.training.ecommerce.domain.discount.TieredDiscountStrategy;
import com.training.ecommerce.dto.CreateCouponRequest;
import com.training.ecommerce.service.CatalogService;
import com.training.ecommerce.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a small demo catalogue and four different coupon campaigns on startup
 * (skipped when the catalogue already has products, so restarts stay idempotent).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CatalogService catalogService;
    private final CouponService couponService;

    public DataSeeder(CatalogService catalogService, CouponService couponService) {
        this.catalogService = catalogService;
        this.couponService = couponService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!catalogService.listAll().isEmpty()) {
            return;
        }

        catalogService.createProduct("Wireless Mouse", "Ergonomic 2.4 GHz wireless mouse",
                com.training.ecommerce.domain.model.ProductCategory.ELECTRONICS,
                new BigDecimal("799.00"), 25);
        var keyboard = catalogService.createProduct("Mechanical Keyboard",
                "Tenkeyless mechanical keyboard with blue switches",
                com.training.ecommerce.domain.model.ProductCategory.ELECTRONICS,
                new BigDecimal("3499.00"), 15);
        catalogService.createProduct("USB-C Hub", "7-in-1 USB-C hub with HDMI and card reader",
                com.training.ecommerce.domain.model.ProductCategory.ELECTRONICS,
                new BigDecimal("1499.00"), 30);
        var cleanCode = catalogService.createProduct("Clean Code (Book)",
                "Robert C. Martin's classic on writing readable code",
                com.training.ecommerce.domain.model.ProductCategory.BOOKS,
                new BigDecimal("549.00"), 40);
        catalogService.createProduct("Effective Java (Book)",
                "Joshua Bloch's best practices for the Java platform",
                com.training.ecommerce.domain.model.ProductCategory.BOOKS,
                new BigDecimal("699.00"), 35);
        catalogService.createProduct("Design Patterns (Book)",
                "The Gang of Four reference on reusable OO design",
                com.training.ecommerce.domain.model.ProductCategory.BOOKS,
                new BigDecimal("899.00"), 20);
        catalogService.createProduct("Cotton T-Shirt", "100% organic cotton, unisex fit",
                com.training.ecommerce.domain.model.ProductCategory.CLOTHING,
                new BigDecimal("499.00"), 60);
        catalogService.createProduct("Denim Jeans", "Classic straight-cut denim jeans",
                com.training.ecommerce.domain.model.ProductCategory.CLOTHING,
                new BigDecimal("1899.00"), 25);
        catalogService.createProduct("Green Tea Pack", "Pack of 25 organic green tea bags",
                com.training.ecommerce.domain.model.ProductCategory.GROCERY,
                new BigDecimal("299.00"), 100);
        catalogService.createProduct("Desk Lamp", "LED desk lamp with adjustable arm",
                com.training.ecommerce.domain.model.ProductCategory.HOME,
                new BigDecimal("1299.00"), 18);

        couponService.create(new CreateCouponRequest("SAVE10", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null, null, null, null));
        couponService.create(new CreateCouponRequest("FLAT100", DiscountType.FLAT_AMOUNT,
                new BigDecimal("100"), null, null, null, null));
        couponService.create(new CreateCouponRequest("BOOK2PLUS1", DiscountType.BUY_X_GET_Y_FREE,
                null, cleanCode.getId(), 2, 1, null));
        couponService.create(new CreateCouponRequest("BIGSPEND", DiscountType.TIERED,
                null, null, null, null,
                List.of(new TieredDiscountStrategy.Tier(new BigDecimal("1000"), new BigDecimal("5")),
                        new TieredDiscountStrategy.Tier(new BigDecimal("2000"), new BigDecimal("10")))));

        log.info("Seeded {} demo products and 4 coupons (SAVE10, FLAT100, BOOK2PLUS1, BIGSPEND)", 10);
    }
}
