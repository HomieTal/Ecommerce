package com.training.ecommerce.service;

import com.training.ecommerce.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceTest {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    @DisplayName("setStock refuses negative levels")
    void setStockRejectsNegative() {
        assertThatThrownBy(() -> inventoryService.setStock("PRD-1", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("getStock treats unknown products as zero and hasStock reflects levels")
    void stockQueriesReflectLevels() {
        assertThat(inventoryService.getStock("PRD-unknown")).isZero();
        assertThat(inventoryService.hasStock("PRD-unknown", 1)).isFalse();

        inventoryService.setStock("PRD-1", 5);
        assertThat(inventoryService.hasStock("PRD-1", 5)).isTrue();
        assertThat(inventoryService.hasStock("PRD-1", 6)).isFalse();
    }

    @Test
    @DisplayName("addStock accumulates on top of the current level")
    void addStockAccumulates() {
        inventoryService.setStock("PRD-1", 5);
        inventoryService.addStock("PRD-1", 7);

        assertThat(inventoryService.getStock("PRD-1")).isEqualTo(12);
    }

    @Test
    @DisplayName("reduceStock is all-or-nothing: a short line leaves everything untouched")
    void reduceStockIsAllOrNothing() {
        inventoryService.setStock("PRD-A", 5);
        inventoryService.setStock("PRD-B", 5);

        Map<String, Integer> request = Map.of("PRD-A", 3, "PRD-B", 8);

        assertThatThrownBy(() -> inventoryService.reduceStock(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("PRD-B");

        assertThat(inventoryService.getStock("PRD-A")).isEqualTo(5);
        assertThat(inventoryService.getStock("PRD-B")).isEqualTo(5);
    }

    @Test
    @DisplayName("reduceStock succeeds when every line fits")
    void reduceStockSucceedsWhenFits() {
        inventoryService.setStock("PRD-A", 5);
        inventoryService.setStock("PRD-B", 5);

        assertThatCode(() -> inventoryService.reduceStock(Map.of("PRD-A", 3, "PRD-B", 5)))
                .doesNotThrowAnyException();

        assertThat(inventoryService.getStock("PRD-A")).isEqualTo(2);
        assertThat(inventoryService.getStock("PRD-B")).isZero();
    }

    @Test
    @DisplayName("restoreStock puts cancelled quantities back into the warehouse")
    void restoreStockRefills() {
        inventoryService.setStock("PRD-A", 2);
        inventoryService.restoreStock(Map.of("PRD-A", 3));

        assertThat(inventoryService.getStock("PRD-A")).isEqualTo(5);
    }
}
