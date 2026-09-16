package com.training.ecommerce.repository.inmemory;

import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory {@link ProductRepository} - the "database" of this training project. */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> productsById = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        Objects.requireNonNull(product, "Product is required");
        productsById.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(productsById.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(productsById.values());
    }

    @Override
    public boolean existsById(String id) {
        return productsById.containsKey(id);
    }
}
