package com.training.ecommerce.web;

import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.dto.CreateProductRequest;
import com.training.ecommerce.dto.ProductResponse;
import com.training.ecommerce.dto.UpdatePriceRequest;
import com.training.ecommerce.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catalogue endpoints: browse, search, filter, sort, create and re-price.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<ProductResponse> listAll() {
        return catalogService.listAll().stream().map(this::toResponse).toList();
    }

    /** Case-insensitive search across name and description: /api/products/search?q=java */
    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam("q") String query) {
        return catalogService.search(query).stream().map(this::toResponse).toList();
    }

    /** /api/products/category/BOOKS */
    @GetMapping("/category/{category}")
    public List<ProductResponse> byCategory(@PathVariable String category) {
        return catalogService.listByCategory(parseCategory(category)).stream().map(this::toResponse).toList();
    }

    private com.training.ecommerce.domain.model.ProductCategory parseCategory(String category) {
        try {
            return com.training.ecommerce.domain.model.ProductCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown product category '%s'. Valid: ELECTRONICS, BOOKS, CLOTHING, GROCERY, HOME, TOYS"
                            .formatted(category));
        }
    }

    /** /api/products/sorted?order=desc */
    @GetMapping("/sorted")
    public List<ProductResponse> sortedByPrice(@RequestParam(defaultValue = "asc") String order) {
        boolean ascending = !"desc".equalsIgnoreCase(order);
        return catalogService.sortedByPrice(ascending).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable String id) {
        return toResponse(catalogService.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
        Product created = catalogService.createProduct(request.name(), request.description(),
                request.category(), request.price(), request.initialStock());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PatchMapping("/{id}/price")
    public ProductResponse updatePrice(@PathVariable String id, @RequestBody UpdatePriceRequest request) {
        return toResponse(catalogService.updatePrice(id, request.price()));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getCategory(), product.getPrice(), catalogService.getStock(product.getId()));
    }
}
