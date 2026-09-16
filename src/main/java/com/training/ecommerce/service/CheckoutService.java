package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.DiscountContext;
import com.training.ecommerce.domain.exception.EmptyCartException;
import com.training.ecommerce.domain.exception.InsufficientStockException;
import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.domain.model.Cart;
import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.PriceSummary;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.repository.CartRepository;
import com.training.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates the checkout: the transaction that turns a cart into an order.
 *
 * <p>The order of operations matters and is the heart of this project's
 * business-rule design:</p>
 * <ol>
 *   <li>verify the cart exists and is not empty</li>
 *   <li>re-resolve every line against the live catalogue (price may have changed)</li>
 *   <li>re-check stock for every line (it may have dropped since "add to cart")</li>
 *   <li>price the cart once, with the coupon if any</li>
 *   <li>create the order (immutable snapshot)</li>
 *   <li>atomically take the stock out</li>
 *   <li>clear the cart</li>
 * </ol>
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final OrderService orderService;

    public CheckoutService(CartRepository cartRepository, ProductRepository productRepository,
                           InventoryService inventoryService, PricingService pricingService,
                           OrderService orderService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.orderService = orderService;
    }

    public Order checkout(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EmptyCartException(customerId));
        if (cart.isEmpty()) {
            throw new EmptyCartException(customerId);
        }

        List<DiscountContext.Line> pricedLines = pricingService.buildLines(cart);
        verifyStock(cart, pricedLines);

        PriceSummary summary = pricingService.summarize(cart);

        Order order = orderService.createOrder(customerId, pricedLines, summary);
        inventoryService.reduceStock(requiredQuantities(cart));

        cart.clear();
        cartRepository.save(cart);

        log.info("Checkout completed for customer {} -> order {} (subtotal {}, discount {}, total {})",
                customerId, order.getId(), summary.subtotal(), summary.discount(), summary.total());
        return order;
    }

    private void verifyStock(Cart cart, List<DiscountContext.Line> pricedLines) {
        Map<String, DiscountContext.Line> linesById = new LinkedHashMap<>();
        pricedLines.forEach(line -> linesById.put(line.productId(), line));

        for (var item : cart.getItems()) {
            int quantity = item.getQuantity();
            int available = inventoryService.getStock(item.getProductId());
            if (quantity > available) {
                String productName = linesById.containsKey(item.getProductId())
                        ? linesById.get(item.getProductId()).productName()
                        : productRepository.findById(item.getProductId())
                                .map(Product::getName)
                                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                throw new InsufficientStockException(productName, quantity, available);
            }
        }
    }

    private Map<String, Integer> requiredQuantities(Cart cart) {
        Map<String, Integer> required = new LinkedHashMap<>();
        cart.getItems().forEach(item ->
                required.merge(item.getProductId(), item.getQuantity(), Integer::sum));
        return required;
    }
}
