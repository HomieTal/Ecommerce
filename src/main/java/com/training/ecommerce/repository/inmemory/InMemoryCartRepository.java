package com.training.ecommerce.repository.inmemory;

import com.training.ecommerce.domain.model.Cart;
import com.training.ecommerce.repository.CartRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory {@link CartRepository}, keyed by customer id. */
@Repository
public class InMemoryCartRepository implements CartRepository {

    private final Map<String, Cart> cartsByCustomer = new ConcurrentHashMap<>();

    @Override
    public Cart save(Cart cart) {
        Objects.requireNonNull(cart, "Cart is required");
        cartsByCustomer.put(cart.getCustomerId(), cart);
        return cart;
    }

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        return Optional.ofNullable(cartsByCustomer.get(customerId));
    }

    @Override
    public void delete(String customerId) {
        cartsByCustomer.remove(customerId);
    }
}
