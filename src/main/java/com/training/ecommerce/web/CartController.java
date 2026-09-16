package com.training.ecommerce.web;

import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.dto.AddItemRequest;
import com.training.ecommerce.dto.ApplyCouponRequest;
import com.training.ecommerce.dto.CartResponse;
import com.training.ecommerce.dto.UpdateQuantityRequest;
import com.training.ecommerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart endpoints. The customer id plays the role of the (simulated) logged-in
 * user; a real app would take it from the security context.
 */
@RestController
@RequestMapping("/api/carts/{customerId}")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse view(@PathVariable String customerId) {
        return cartService.viewCart(customerId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable String customerId,
                                                @RequestBody AddItemRequest request) {
        cartService.addItem(customerId, request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.viewCart(customerId));
    }

    @PatchMapping("/items/{productId}")
    public CartResponse updateQuantity(@PathVariable String customerId,
                                       @PathVariable String productId,
                                       @RequestBody UpdateQuantityRequest request) {
        cartService.updateQuantity(customerId, productId, request.quantity());
        return cartService.viewCart(customerId);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@PathVariable String customerId, @PathVariable String productId) {
        cartService.removeItem(customerId, productId);
        return cartService.viewCart(customerId);
    }

    @PostMapping("/coupon")
    public CartResponse applyCoupon(@PathVariable String customerId,
                                    @RequestBody ApplyCouponRequest request) {
        cartService.applyCoupon(customerId, request.code());
        return cartService.viewCart(customerId);
    }

    @DeleteMapping("/coupon")
    public CartResponse removeCoupon(@PathVariable String customerId) {
        cartService.removeCoupon(customerId);
        return cartService.viewCart(customerId);
    }

    @DeleteMapping
    public CartResponse clear(@PathVariable String customerId) {
        cartService.clearCart(customerId);
        return cartService.viewCart(customerId);
    }
}
