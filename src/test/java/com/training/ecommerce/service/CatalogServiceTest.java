package com.training.ecommerce.service;

import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.domain.model.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogServiceTest {

    private TestSupport.Services services;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        services = TestSupport.fresh();
        catalogService = services.catalogService();
    }

    @Test
    @DisplayName("createProduct registers the product and opens its stock position")
    void createProductRegistersProductAndStock() {
        Product product = catalogService.createProduct("Wireless Mouse", "2.4 GHz mouse",
                ProductCategory.ELECTRONICS, new java.math.BigDecimal("799.00"), 25);

        assertThat(product.getId()).isNotBlank();
        assertThat(catalogService.getProduct(product.getId()).getName()).isEqualTo("Wireless Mouse");
        assertThat(catalogService.getStock(product.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("getProduct throws ProductNotFoundException for unknown ids")
    void getProductUnknownIdThrows() {
        assertThatThrownBy(() -> catalogService.getProduct("PRD-9999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PRD-9999");
    }

    @Test
    @DisplayName("search matches name and description case-insensitively")
    void searchMatchesNameAndDescription() {
        services.product("Clean Code", "Robert C. Martin classic", ProductCategory.BOOKS, "549.00", 10);
        services.product("Green Tea", "Organic herbal drink", ProductCategory.GROCERY, "299.00", 10);

        List<Product> byName = catalogService.search("CLEAN");
        List<Product> byDescription = catalogService.search("herbal");

        assertThat(byName).extracting(Product::getName).containsExactly("Clean Code");
        assertThat(byDescription).extracting(Product::getName).containsExactly("Green Tea");
    }

    @Test
    @DisplayName("search with no match returns an empty list, not an exception")
    void searchNoMatchReturnsEmptyList() {
        services.product("Desk Lamp", "LED lamp", ProductCategory.HOME, "1299.00", 5);

        assertThat(catalogService.search("microscope")).isEmpty();
    }

    @Test
    @DisplayName("listByCategory filters products by their enum category")
    void listByCategoryFilters() {
        services.product("T-Shirt", "cotton", ProductCategory.CLOTHING, "499.00", 10);
        services.product("Jeans", "denim", ProductCategory.CLOTHING, "1899.00", 10);
        services.product("Mouse", "wireless", ProductCategory.ELECTRONICS, "799.00", 10);

        assertThat(catalogService.listByCategory(ProductCategory.CLOTHING))
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("T-Shirt", "Jeans");
    }

    @Test
    @DisplayName("sortedByPrice orders products ascending or descending")
    void sortedByPriceOrdersBothDirections() {
        services.product("Cheap", "budget pick", ProductCategory.HOME, "100.00", 5);
        services.product("Middle", "mid-range pick", ProductCategory.HOME, "500.00", 5);
        services.product("Pricey", "premium pick", ProductCategory.HOME, "900.00", 5);

        List<Product> ascending = catalogService.sortedByPrice(true);
        List<Product> descending = catalogService.sortedByPrice(false);

        assertThat(ascending).extracting(Product::getName)
                .containsExactly("Cheap", "Middle", "Pricey");
        assertThat(descending).extracting(Product::getName)
                .containsExactly("Pricey", "Middle", "Cheap");
    }

    @Test
    @DisplayName("updatePrice rejects zero and negative prices")
    void updatePriceRejectsNonPositive() {
        Product product = services.product("Desk Lamp", "LED lamp", ProductCategory.HOME, "1299.00", 5);

        assertThatThrownBy(() -> catalogService.updatePrice(product.getId(), java.math.BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> catalogService.updatePrice(product.getId(), new java.math.BigDecimal("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
