package com.training.ecommerce.service;

import com.training.ecommerce.domain.exception.CouponNotFoundException;
import com.training.ecommerce.domain.exception.InsufficientStockException;
import com.training.ecommerce.domain.exception.InvalidQuantityException;
import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.domain.exception.ProductNotInCartException;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import com.training.ecommerce.dto.CartResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartServiceTest {

    private TestSupport.Services services;
    private CartService cartService;
    private Product mouse;

    @BeforeEach
    void setUp() {
        services = TestSupport.fresh();
        cartService = services.cartService();
        mouse = services.product("Wireless Mouse", "2.4 GHz mouse",
                ProductCategory.ELECTRONICS, "799.00", 5);
    }

    @Test
    @DisplayName("addItem merges repeated adds of the same product into one line")
    void addItemMergesQuantities() {
        cartService.addItem("alice", mouse.getId(), 1);
        cartService.addItem("alice", mouse.getId(), 2);

        CartResponse cart = cartService.viewCart("alice");
        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("addItem rejects quantities below one")
    void addItemRejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> cartService.addItem("alice", mouse.getId(), 0))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("addItem rejects unknown products")
    void addItemRejectsUnknownProduct() {
        assertThatThrownBy(() -> cartService.addItem("alice", "PRD-9999", 1))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("addItem rejects quantities beyond available stock (cart + new units)")
    void addItemRejectsBeyondStock() {
        cartService.addItem("alice", mouse.getId(), 4);

        assertThatThrownBy(() -> cartService.addItem("alice", mouse.getId(), 2))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 6");
    }

    @Test
    @DisplayName("updateQuantity validates stock and cart membership")
    void updateQuantityValidates() {
        cartService.addItem("alice", mouse.getId(), 1);

        assertThatThrownBy(() -> cartService.updateQuantity("alice", mouse.getId(), 99))
                .isInstanceOf(InsufficientStockException.class);
        assertThatThrownBy(() -> cartService.updateQuantity("alice", "PRD-unknown", 1))
                .isInstanceOf(ProductNotInCartException.class);
    }

    @Test
    @DisplayName("removeItem throws when the product is not in the cart")
    void removeItemNotInCartThrows() {
        assertThatThrownBy(() -> cartService.removeItem("alice", mouse.getId()))
                .isInstanceOf(ProductNotInCartException.class);
    }

    @Test
    @DisplayName("applyCoupon rejects unknown codes")
    void applyCouponUnknownCodeThrows() {
        assertThatThrownBy(() -> cartService.applyCoupon("alice", "NOSUCH"))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    @DisplayName("viewCart prices the cart against the live catalogue")
    void viewCartComputesTotals() {
        Product tea = services.product("Green Tea", "25 bags", ProductCategory.GROCERY, "299.00", 10);
        cartService.addItem("alice", mouse.getId(), 2);
        cartService.addItem("alice", tea.getId(), 1);

        CartResponse cart = cartService.viewCart("alice");

        assertThat(cart.subtotal()).isEqualByComparingTo(new BigDecimal("1897.00")); // 2*799 + 299
        assertThat(cart.discount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(cart.total()).isEqualByComparingTo(new BigDecimal("1897.00"));
        assertThat(cart.totalQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("clearCart empties every line and drops the coupon")
    void clearCartEmptiesEverything() {
        cartService.addItem("alice", mouse.getId(), 2);
        services.coupon("SAVE10", com.training.ecommerce.domain.discount.DiscountType.PERCENTAGE, "10");
        cartService.applyCoupon("alice", "SAVE10");

        cartService.clearCart("alice");

        CartResponse cart = cartService.viewCart("alice");
        assertThat(cart.items()).isEmpty();
        assertThat(cart.couponCode()).isNull();
    }
}
