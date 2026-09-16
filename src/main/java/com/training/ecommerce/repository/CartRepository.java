package com.training.ecommerce.repository;

import com.training.ecommerce.domain.model.Cart;

import java.util.Optional;

/** Persistence contract for customer carts (one cart per customer id). */
public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findByCustomerId(String customerId);

    void delete(String customerId);
}
