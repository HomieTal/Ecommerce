package com.training.ecommerce.service;

import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.OrderStatus;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {

    private TestSupport.Services services;
    private OrderService orderService;
    private Product mouse;

    @BeforeEach
    void setUp() {
        services = TestSupport.fresh();
        orderService = services.orderService();
        mouse = services.product("Wireless Mouse", "2.4 GHz mouse",
                ProductCategory.ELECTRONICS, "799.00", 10);
    }

    private Order placeOrder(String customerId, int quantity) {
        services.cartService().addItem(customerId, mouse.getId(), quantity);
        return services.checkoutService().checkout(customerId);
    }

    @Test
    @DisplayName("order history lists the newest orders first")
    void historyIsNewestFirst() {
        placeOrder("alice", 1); // ORD-1001
        placeOrder("alice", 2); // ORD-1002

        List<Order> history = orderService.getOrderHistory("alice");

        assertThat(history).extracting(Order::getId)
                .containsExactly("ORD-1002", "ORD-1001");
    }

    @Test
    @DisplayName("getOrder throws OrderNotFoundException for unknown ids")
    void unknownOrderThrows() {
        assertThatThrownBy(() -> orderService.getOrder("ORD-9999"))
                .isInstanceOf(com.training.ecommerce.domain.exception.OrderNotFoundException.class);
    }

    @Test
    @DisplayName("cancelling a confirmed order returns the goods to the warehouse")
    void cancelRestocksInventory() {
        Order order = placeOrder("alice", 3);
        assertThat(services.inventoryService().getStock(mouse.getId())).isEqualTo(7);

        Order cancelled = orderService.cancelOrder(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(services.inventoryService().getStock(mouse.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("a shipped order can no longer be cancelled")
    void shippedOrderCannotBeCancelled() {
        Order order = placeOrder("alice", 1);
        orderService.transitionStatus(order.getId(), OrderStatus.SHIPPED);

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer be cancelled");
    }

    @Test
    @DisplayName("invalid lifecycle transitions are rejected by the OrderStatus rules")
    void invalidTransitionsRejected() {
        Order order = placeOrder("alice", 1);

        // CONFIRMED -> DELIVERED skips SHIPPED
        assertThatThrownBy(() -> orderService.transitionStatus(order.getId(), OrderStatus.DELIVERED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONFIRMED -> DELIVERED");

        // the legal path works: CONFIRMED -> SHIPPED -> DELIVERED
        orderService.transitionStatus(order.getId(), OrderStatus.SHIPPED);
        Order delivered = orderService.transitionStatus(order.getId(), OrderStatus.DELIVERED);

        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.getStatus().isTerminal()).isTrue();
    }

    @Test
    @DisplayName("history only contains the requesting customer's orders")
    void historyIsPerCustomer() {
        placeOrder("alice", 1);
        placeOrder("bob", 2);

        assertThat(orderService.getOrderHistory("alice")).hasSize(1);
        assertThat(orderService.getOrderHistory("bob")).hasSize(1);
    }
}
