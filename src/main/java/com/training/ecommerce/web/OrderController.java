package com.training.ecommerce.web;

import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.OrderStatus;
import com.training.ecommerce.dto.OrderLineResponse;
import com.training.ecommerce.dto.OrderResponse;
import com.training.ecommerce.dto.StatusUpdateRequest;
import com.training.ecommerce.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order endpoints: history, lookup, cancellation and lifecycle transitions.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Order history of one customer, newest first. */
    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> history(@PathVariable String customerId) {
        return orderService.getOrderHistory(customerId).stream().map(OrderController::toResponse).toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOne(@PathVariable String orderId) {
        return toResponse(orderService.getOrder(orderId));
    }

    /** Cancels the order and returns the goods to the warehouse. */
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable String orderId) {
        return toResponse(orderService.cancelOrder(orderId));
    }

    /** Body: {"status": "SHIPPED"} - invalid transitions are rejected with 409. */
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable String orderId,
                                      @RequestBody StatusUpdateRequest request) {
        OrderStatus target = parseStatus(request.status());
        return toResponse(orderService.transitionStatus(orderId, target));
    }

    static OrderResponse toResponse(Order order) {
        List<OrderLineResponse> lines = order.getItems().stream()
                .map(item -> new OrderLineResponse(item.productId(), item.productName(), item.category(),
                        item.unitPrice(), item.quantity(), item.lineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomerId(), lines,
                order.getSubtotal(), order.getDiscount(), order.getTotal(),
                order.getCouponCode(), order.getStatus(), order.getCreatedAt());
    }

    private static OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status == null ? "" : status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown order status '%s'. Valid: CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
                            .formatted(status));
        }
    }
}
