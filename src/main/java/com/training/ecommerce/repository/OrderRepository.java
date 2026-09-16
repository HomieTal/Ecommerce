package com.training.ecommerce.repository;

import com.training.ecommerce.domain.model.Order;

import java.util.List;
import java.util.Optional;

/** Persistence contract for placed orders. */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String id);

    List<Order> findByCustomerId(String customerId);

    /** Generates the next unique, human-readable order id (e.g. ORD-1001). */
    String nextOrderId();
}
