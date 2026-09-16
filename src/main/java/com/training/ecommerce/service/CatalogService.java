package com.training.ecommerce.service;

import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import com.training.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Read-mostly catalogue operations. Notice how much of the work is expressed
 * with the Streams API: filtering by keyword, filtering by category and
 * sorting by price are all one-liners over the repository contents.
 */
@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AtomicLong idSequence = new AtomicLong(1000);

    public CatalogService(ProductRepository productRepository, InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    /** Registers a new product and opens its warehouse position in one step. */
    public Product createProduct(String name, String description, ProductCategory category,
                                 java.math.BigDecimal price, int initialStock) {
        Product product = new Product(nextProductId(), name, description, category, price);
        Product saved = productRepository.save(product);
        inventoryService.setStock(saved.getId(), Math.max(0, initialStock));
        return saved;
    }

    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public List<Product> listAll() {
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getName))
                .toList();
    }

    /** Case-insensitive keyword search across name and description. */
    public List<Product> search(String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase();
        if (query.isEmpty()) {
            return listAll();
        }
        return listAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(query)
                        || product.getDescription().toLowerCase().contains(query))
                .toList();
    }

    public List<Product> listByCategory(ProductCategory category) {
        return listAll().stream()
                .filter(product -> product.getCategory() == category)
                .toList();
    }

    public List<Product> sortedByPrice(boolean ascending) {
        Comparator<Product> byPrice = Comparator.comparing(Product::getPrice);
        return listAll().stream()
                .sorted(ascending ? byPrice : byPrice.reversed())
                .toList();
    }

    public Product updatePrice(String productId, java.math.BigDecimal newPrice) {
        Product product = getProduct(productId);
        product.updatePrice(newPrice);
        return productRepository.save(product);
    }

    public int getStock(String productId) {
        getProduct(productId); // 404 for unknown ids
        return inventoryService.getStock(productId);
    }

    private String nextProductId() {
        return "PRD-" + idSequence.incrementAndGet();
    }
}
