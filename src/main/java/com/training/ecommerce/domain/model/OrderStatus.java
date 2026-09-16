package com.training.ecommerce.domain.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle of an order, modelled as an enum that owns its own transition
 * rules (a state machine in miniature).
 *
 * <pre>
 *   CONFIRMED ──► SHIPPED ──► DELIVERED
 *       │
 *       └──────► CANCELLED
 * </pre>
 *
 * <p>Keeping {@code canTransitionTo} on the enum means the rule lives next to
 * the values it governs - callers cannot invent invalid transitions.</p>
 */
public enum OrderStatus {

    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Allowed transitions per status. Declared after the constants (enum
     * constructors must not reference sibling constants directly), so this
     * static field can safely refer to all four values.
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TARGETS = buildTransitions();

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(CONFIRMED, EnumSet.of(SHIPPED, CANCELLED));
        transitions.put(SHIPPED, EnumSet.of(DELIVERED));
        transitions.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        transitions.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        return transitions;
    }

    /** @return true if this status may legally move to {@code target}. */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TARGETS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(target);
    }

    /** @return true once the order can no longer change state. */
    public boolean isTerminal() {
        return ALLOWED_TARGETS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).isEmpty();
    }

    /** @return true if the order may still be cancelled (stock can be returned). */
    public boolean isCancellable() {
        return this == CONFIRMED;
    }
}
