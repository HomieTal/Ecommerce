package com.training.ecommerce.service;

import com.training.ecommerce.domain.discount.DiscountContext;
import com.training.ecommerce.domain.exception.OrderNotFoundException;
import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.OrderItem;
import com.training.ecommerce.domain.model.OrderStatus;
import com.training.ecommerce.domain.model.PriceSummary;
import com.training.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Order creation, history and lifecycle. Orders own an immutable snapshot of
 * the purchased items; cancelling a still-cancellable order returns the goods
 * to the warehouse via the {@link InventoryService}.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    public Order createOrder(String customerId, List<DiscountContext.Line> pricedLines, PriceSummary summary) {
        List<OrderItem> items = pricedLines.stream()
                .map(line -> new OrderItem(line.productId(), line.productName(), line.category(),
                        line.unitPrice(), line.quantity()))
                .toList();

        Order order = new Order(orderRepository.nextOrderId(), customerId, items,
                summary.subtotal(), summary.discount(), summary.couponCode(), Instant.now());
        Order saved = orderRepository.save(order);
        log.info("Order {} placed by customer {} (total {})", saved.getId(), customerId, saved.getTotal());
        return saved;
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /** Newest orders first; ties (same timestamp) break on id, newest first. */
    public List<Order> getOrderHistory(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed()
                        .thenComparing(Order::getId, Comparator.reverseOrder()))
                .toList();
    }

    public Order cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        order.cancel(); // IllegalStateException when no longer cancellable
        inventoryService.restoreStock(quantityByProduct(order));
        log.info("Order {} cancelled - stock restored", orderId);
        return orderRepository.save(order);
    }

    public Order transitionStatus(String orderId, OrderStatus target) {
        Order order = getOrder(orderId);
        order.transitionTo(target);
        return orderRepository.save(order);
    }

    private Map<String, Integer> quantityByProduct(Order order) {
        return order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::productId, OrderItem::quantity, Integer::sum));
    }
}
