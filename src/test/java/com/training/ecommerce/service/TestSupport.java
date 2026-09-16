package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.DiscountType;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import com.training.ecommerce.dto.CreateCouponRequest;
import com.training.ecommerce.repository.CartRepository;
import com.training.ecommerce.repository.CouponRepository;
import com.training.ecommerce.repository.OrderRepository;
import com.training.ecommerce.repository.ProductRepository;
import com.training.ecommerce.repository.inmemory.InMemoryCartRepository;
import com.training.ecommerce.repository.inmemory.InMemoryCouponRepository;
import com.training.ecommerce.repository.inmemory.InMemoryOrderRepository;
import com.training.ecommerce.repository.inmemory.InMemoryProductRepository;

import java.math.BigDecimal;

/**
 * Builds a freshly wired object graph for each test - the same wiring Spring
 * would perform at runtime, but without starting an application context.
 * Unit-test speed with integration-like realism.
 */
public final class TestSupport {

    private TestSupport() {
    }

    public record Services(ProductRepository productRepository, CartRepository cartRepository,
                           OrderRepository orderRepository, CouponRepository couponRepository,
                           InventoryService inventoryService, CatalogService catalogService,
                           PricingService pricingService, CartService cartService,
                           CouponService couponService, OrderService orderService,
                           CheckoutService checkoutService) {

        public Product product(String name, String description, ProductCategory category,
                               String price, int stock) {
            return catalogService.createProduct(name, description, category,
                    new BigDecimal(price), stock);
        }

        public void coupon(String code, DiscountType type, String value) {
            couponService.create(new CreateCouponRequest(code, type, new BigDecimal(value),
                    null, null, null, null));
        }
    }

    public static Services fresh() {
        ProductRepository productRepository = new InMemoryProductRepository();
        CartRepository cartRepository = new InMemoryCartRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();
        CouponRepository couponRepository = new InMemoryCouponRepository();

        InventoryService inventoryService = new InventoryService();
        CatalogService catalogService = new CatalogService(productRepository, inventoryService);
        PricingService pricingService = new PricingService(productRepository, couponRepository);
        CartService cartService = new CartService(cartRepository, productRepository,
                couponRepository, inventoryService, pricingService);
        CouponService couponService = new CouponService(couponRepository);
        OrderService orderService = new OrderService(orderRepository, inventoryService);
        CheckoutService checkoutService = new CheckoutService(cartRepository, productRepository,
                inventoryService, pricingService, orderService);

        return new Services(productRepository, cartRepository, orderRepository, couponRepository,
                inventoryService, catalogService, pricingService, cartService, couponService,
                orderService, checkoutService);
    }
}
