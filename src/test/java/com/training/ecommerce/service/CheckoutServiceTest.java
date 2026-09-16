package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.DiscountType;
import com.training.ecommerce.domain.discount.TieredDiscountStrategy;
import com.training.ecommerce.domain.exception.EmptyCartException;
import com.training.ecommerce.domain.exception.InsufficientStockException;
import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import com.training.ecommerce.dto.CreateCouponRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutServiceTest {

    private TestSupport.Services services;
    private CheckoutService checkoutService;
    private Product mouse;
    private Product keyboard;

    @BeforeEach
    void setUp() {
        services = TestSupport.fresh();
        checkoutService = services.checkoutService();
        mouse = services.product("Wireless Mouse", "2.4 GHz mouse",
                ProductCategory.ELECTRONICS, "799.00", 25);
        keyboard = services.product("Mechanical Keyboard", "blue switches",
                ProductCategory.ELECTRONICS, "3499.00", 15);
    }

    @Test
    @DisplayName("checkout creates the order, empties the cart and reduces stock")
    void checkoutHappyPath() {
        addItem("alice", mouse, 1);
        addItem("alice", keyboard, 1);

        Order order = checkoutService.checkout("alice");

        assertThat(order.getId()).startsWith("ORD-");
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("4298.00"));
        assertThat(order.getCustomerId()).isEqualTo("alice");

        // cart emptied
        assertThat(services.cartService().viewCart("alice").items()).isEmpty();
        // stock reduced
        assertThat(services.inventoryService().getStock(mouse.getId())).isEqualTo(24);
        assertThat(services.inventoryService().getStock(keyboard.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("checkout with an empty cart is rejected")
    void checkoutEmptyCartThrows() {
        assertThatThrownBy(() -> checkoutService.checkout("alice"))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("stock dropping after add-to-cart aborts checkout and keeps the cart intact")
    void checkoutWithDroppedStockAborts() {
        addItem("alice", mouse, 5);
        services.inventoryService().setStock(mouse.getId(), 2); // someone else bought the rest

        assertThatThrownBy(() -> checkoutService.checkout("alice"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("available 2");

        // nothing was ordered, nothing was lost
        assertThat(services.orderRepository().findByCustomerId("alice")).isEmpty();
        assertThat(services.cartService().viewCart("alice").items()).isNotEmpty();
        assertThat(services.inventoryService().getStock(mouse.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("percentage coupon: SAVE10 takes 10% off the subtotal")
    void percentageCouponDiscount() {
        services.coupon("SAVE10", DiscountType.PERCENTAGE, "10");
        addItem("alice", mouse, 2); // 2 x 799 = 1598

        Order order = checkoutWithCoupon("alice", "SAVE10");

        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("1598.00"));
        assertThat(order.getDiscount()).isEqualByComparingTo(new BigDecimal("159.80"));
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("1438.20"));
        assertThat(order.getCouponCode()).isEqualTo("SAVE10");
    }

    @Test
    @DisplayName("flat coupon is capped at the subtotal: a cart cheaper than the coupon costs zero")
    void flatCouponCappedAtSubtotal() {
        services.coupon("FLAT100", DiscountType.FLAT_AMOUNT, "100");
        Product tea = services.product("Green Tea", "25 bags", ProductCategory.GROCERY, "99.00", 10);
        addItem("alice", tea, 1);

        Order order = checkoutWithCoupon("alice", "FLAT100");

        assertThat(order.getDiscount()).isEqualByComparingTo(new BigDecimal("99.00"));
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("buy-2-get-1-free: three units pay for two")
    void buyTwoGetOneFreeApplied() {
        Product book = services.product("Clean Code", "Robert C. Martin",
                ProductCategory.BOOKS, "549.00", 40);
        services.couponService().create(new CreateCouponRequest("BOOK2PLUS1",
                DiscountType.BUY_X_GET_Y_FREE, null, book.getId(), 2, 1, null));
        addItem("alice", book, 3);

        Order order = checkoutWithCoupon("alice", "BOOK2PLUS1");

        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("1647.00"));
        assertThat(order.getDiscount()).isEqualByComparingTo(new BigDecimal("549.00"));
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("1098.00"));
    }

    @Test
    @DisplayName("tiered coupon: the highest matching bracket wins")
    void tieredCouponAppliesHighestBracket() {
        services.couponService().create(new CreateCouponRequest("BIGSPEND", DiscountType.TIERED,
                null, null, null, null,
                List.of(new TieredDiscountStrategy.Tier(new BigDecimal("1000"), new BigDecimal("5")),
                        new TieredDiscountStrategy.Tier(new BigDecimal("2000"), new BigDecimal("10")))));
        addItem("alice", keyboard, 1); // 3499 -> 10% bracket

        Order order = checkoutWithCoupon("alice", "BIGSPEND");

        assertThat(order.getDiscount()).isEqualByComparingTo(new BigDecimal("349.90"));
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("3149.10"));
    }

    @Test
    @DisplayName("a coupon deactivated after being applied fails the checkout")
    void deactivatedCouponFailsCheckout() {
        services.coupon("SAVE10", DiscountType.PERCENTAGE, "10");
        addItem("alice", mouse, 1);
        services.cartService().applyCoupon("alice", "SAVE10");
        services.couponService().setActive("SAVE10", false);

        assertThatThrownBy(() -> checkoutService.checkout("alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer active");

        assertThat(services.orderRepository().findByCustomerId("alice")).isEmpty();
    }

    @Test
    @DisplayName("order items snapshot name and unit price at purchase time")
    void orderItemsAreSnapshots() {
        addItem("alice", mouse, 1);

        Order order = checkoutService.checkout("alice");
        services.catalogService().updatePrice(mouse.getId(), new BigDecimal("999.00"));

        assertThat(order.getItems().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("799.00"));
        assertThat(order.getItems().get(0).productName()).isEqualTo("Wireless Mouse");
    }

    /** Small readability helper so tests read like the story they verify. */
    private void addItem(String customerId, Product product, int quantity) {
        services.cartService().addItem(customerId, product.getId(), quantity);
    }

    private Order checkoutWithCoupon(String customerId, String code) {
        services.cartService().applyCoupon(customerId, code);
        return checkoutService.checkout(customerId);
    }
}
