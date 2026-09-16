package com.training.ecommerce.domain.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Buy X of a specific product, get Y of the same product free -
 * for every complete set of (X + Y) units in the cart.
 *
 * <p>Example: buy 2 get 1 free with 7 units in the cart gives
 * {@code 7 / 3 = 2} complete sets, i.e. 2 free units.</p>
 */
public class BuyXGetFreeStrategy implements DiscountStrategy {

    private final String targetProductId;
    private final int buyX;
    private final int freeY;

    public BuyXGetFreeStrategy(String targetProductId, int buyX, int freeY) {
        if (targetProductId == null || targetProductId.isBlank()) {
            throw new IllegalArgumentException("Target product id is required");
        }
        if (buyX < 1) {
            throw new IllegalArgumentException("buyX must be at least 1");
        }
        if (freeY < 1) {
            throw new IllegalArgumentException("freeY must be at least 1");
        }
        this.targetProductId = targetProductId;
        this.buyX = buyX;
        this.freeY = freeY;
    }

    @Override
    public BigDecimal computeDiscount(DiscountContext context) {
        return context.lines().stream()
                .filter(line -> targetProductId.equals(line.productId()))
                .findFirst()
                .map(this::discountForLine)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal discountForLine(DiscountContext.Line line) {
        int setSize = buyX + freeY;
        int completeSets = line.quantity() / setSize;
        int freeUnits = completeSets * freeY;
        if (freeUnits == 0) {
            return BigDecimal.ZERO;
        }
        return line.unitPrice()
                .multiply(BigDecimal.valueOf(freeUnits))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return "Buy %d get %d free on product %s".formatted(buyX, freeY, targetProductId);
    }
}
