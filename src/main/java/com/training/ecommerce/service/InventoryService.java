package com.training.ecommerce.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns warehouse stock levels, deliberately separated from the catalogue.
 *
 * <p>Key business rule: {@link #reduceStock(Map)} is <b>all-or-nothing</b>.
 * A checkout must never partially reserve stock - either every line fits the
 * available quantity, or nothing is taken out of the warehouse at all.</p>
 */
@Service
public class InventoryService {

    private final Map<String, Integer> stockByProduct = new ConcurrentHashMap<>();
    private final ReentrantLock reservationLock = new ReentrantLock();

    /** Sets an absolute stock level; used by seeding and admin operations. */
    public void setStock(String productId, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product id must not be blank");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative, got " + quantity);
        }
        stockByProduct.put(productId, quantity);
    }

    /** Adds units to the current stock (restocking, returned cancellations). */
    public void addStock(String productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Restock quantity must be at least 1, got " + quantity);
        }
        reservationLock.lock();
        try {
            setStock(productId, getStock(productId) + quantity);
        } finally {
            reservationLock.unlock();
        }
    }

    public int getStock(String productId) {
        return stockByProduct.getOrDefault(productId, 0);
    }

    public boolean hasStock(String productId, int quantity) {
        return getStock(productId) >= quantity;
    }

    /**
     * Atomically removes the requested quantities. If any product falls short,
     * an {@code InsufficientStockException} is thrown and nothing is removed.
     */
    public void reduceStock(Map<String, Integer> requestedByProduct) {
        reservationLock.lock();
        try {
            // First pass: verify every line fits.
            for (var entry : requestedByProduct.entrySet()) {
                int requested = entry.getValue();
                int available = getStock(entry.getKey());
                if (requested < 1) {
                    throw new IllegalArgumentException("Requested quantity must be at least 1");
                }
                if (requested > available) {
                    throw new com.training.ecommerce.domain.exception.InsufficientStockException(
                            entry.getKey(), requested, available);
                }
            }
            // Second pass: actually take the units out.
            requestedByProduct.forEach((productId, requested) ->
                    setStock(productId, getStock(productId) - requested));
        } finally {
            reservationLock.unlock();
        }
    }

    /** Puts units back into the warehouse (used when orders are cancelled). */
    public void restoreStock(Map<String, Integer> quantitiesByProduct) {
        quantitiesByProduct.forEach(this::addStock);
    }
}
