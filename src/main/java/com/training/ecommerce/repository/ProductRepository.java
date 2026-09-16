package com.training.ecommerce.repository;

import com.training.ecommerce.domain.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for products. Services depend on this interface, not
 * on the in-memory implementation - swapping in a database later means writing
 * one new class, without touching any business logic.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    List<Product> findAll();

    boolean existsById(String id);
}
