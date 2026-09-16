package com.training.ecommerce.repository.inmemory;

import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Thread-safe in-memory {@link OrderRepository} with sequential order ids. */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> ordersById = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1000);

    @Override
    public Order save(Order order) {
        Objects.requireNonNull(order, "Order is required");
        ordersById.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return ordersById.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }

    @Override
    public String nextOrderId() {
        return "ORD-" + idSequence.incrementAndGet();
    }
}
