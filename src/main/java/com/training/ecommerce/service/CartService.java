package com.training.ecommerce.service;

import com.training.ecommerce.domain.exception.CouponNotFoundException;
import com.training.ecommerce.domain.exception.EmptyCartException;
import com.training.ecommerce.domain.exception.InsufficientStockException;
import com.training.ecommerce.domain.exception.InvalidQuantityException;
import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.domain.exception.ProductNotInCartException;
import com.training.ecommerce.domain.model.Cart;
import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.dto.CartLineResponse;
import com.training.ecommerce.dto.CartResponse;
import com.training.ecommerce.repository.CartRepository;
import com.training.ecommerce.repository.CouponRepository;
import com.training.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Cart use-cases. Every mutation re-validates business rules against the
 * *current* stock level, so a cart can never quietly hold more units than the
 * warehouse can ship.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       CouponRepository couponRepository, InventoryService inventoryService,
                       PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
    }

    public Cart getOrCreateCart(String customerId) {
        requireCustomerId(customerId);
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(new Cart(customerId)));
    }

    /** The priced cart shown to the customer. */
    public CartResponse viewCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        if (cart.isEmpty()) {
            return new CartResponse(customerId, List.of(), 0, null, null,
                    java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
        }
        var summary = pricingService.summarize(cart);
        List<CartLineResponse> lines = pricingService.buildLines(cart).stream()
                .map(line -> new CartLineResponse(line.productId(), line.productName(), line.category(),
                        line.unitPrice(), line.quantity(), line.lineTotal()))
                .toList();
        return new CartResponse(customerId, lines, cart.totalQuantity(), summary.couponCode(),
                summary.couponDescription(), summary.subtotal(), summary.discount(), summary.total());
    }

    public void addItem(String customerId, String productId, int quantity) {
        if (quantity < 1) {
            throw new InvalidQuantityException(quantity);
        }
        Product product = requireProduct(productId);
        Cart cart = getOrCreateCart(customerId);

        int alreadyInCart = cart.findItem(productId).map(item -> item.getQuantity()).orElse(0);
        int requestedTotal = alreadyInCart + quantity;
        int available = inventoryService.getStock(productId);
        if (requestedTotal > available) {
            throw new InsufficientStockException(product.getName(), requestedTotal, available);
        }
        cart.addItem(productId, quantity);
        cartRepository.save(cart);
    }

    public void updateQuantity(String customerId, String productId, int quantity) {
        if (quantity < 1) {
            throw new InvalidQuantityException(quantity);
        }
        Cart cart = requireExistingCart(customerId);
        // cart membership first: the customer's cart is the context they act on
        cart.findItem(productId).orElseThrow(() -> new ProductNotInCartException(productId));
        Product product = requireProduct(productId);

        int available = inventoryService.getStock(productId);
        if (quantity > available) {
            throw new InsufficientStockException(product.getName(), quantity, available);
        }
        cart.updateQuantity(productId, quantity);
        cartRepository.save(cart);
    }

    public void removeItem(String customerId, String productId) {
        Cart cart = getOrCreateCart(customerId);
        cart.removeItem(productId); // throws ProductNotInCartException when absent
        cartRepository.save(cart);
    }

    public Coupon applyCoupon(String customerId, String code) {
        if (code == null || code.isBlank()) {
            throw new CouponNotFoundException("");
        }
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException(code));
        if (!coupon.isActive()) {
            throw new IllegalStateException("Coupon '%s' is no longer active".formatted(coupon.getCode()));
        }
        Cart cart = getOrCreateCart(customerId);
        cart.setCouponCode(coupon.getCode());
        cartRepository.save(cart);
        return coupon;
    }

    public void removeCoupon(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.setCouponCode(null);
        cartRepository.save(cart);
    }

    public void clearCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.clear();
        cartRepository.save(cart);
    }

    /** Guard used by checkout. */
    public Cart requireExistingCart(String customerId) {
        requireCustomerId(customerId);
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EmptyCartException(customerId));
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void requireCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be blank");
        }
    }
}
