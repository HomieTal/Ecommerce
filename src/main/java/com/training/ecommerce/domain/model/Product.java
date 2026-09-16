package com.training.ecommerce.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A catalogue product.
 *
 * <p>Money is represented with {@link BigDecimal} (never double/float) and is
 * normalised to 2 decimal places. Stock is intentionally <b>not</b> stored on
 * the product: it belongs to the {@code InventoryService}, so the catalogue and
 * the warehouse concerns stay separated.</p>
 */
public class Product {

    private final String id;
    private String name;
    private String description;
    private ProductCategory category;
    private BigDecimal price;

    public Product(String id, String name, String description, ProductCategory category, BigDecimal price) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("Product category is required");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
        this.category = category;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Business rule: a price can only be changed to a strictly positive value. */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        this.price = newPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "%s (%s) %s".formatted(id, category, name);
    }
}
