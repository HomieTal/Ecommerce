package com.training.ecommerce.console;

import com.training.ecommerce.domain.exception.ECommerceException;
import com.training.ecommerce.domain.model.Coupon;
import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.domain.model.OrderStatus;
import com.training.ecommerce.domain.model.Product;
import com.training.ecommerce.dto.CartResponse;
import com.training.ecommerce.service.CatalogService;
import com.training.ecommerce.service.CartService;
import com.training.ecommerce.service.CheckoutService;
import com.training.ecommerce.service.CouponService;
import com.training.ecommerce.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven console demo that talks to the same services as the REST layer.
 *
 * <p>Enabled with {@code --app.console.enabled=true}. Combine with
 * {@code --spring.main.web-application-type=none} to run without the web server.</p>
 */
@Component
@ConditionalOnProperty(name = "app.console.enabled", havingValue = "true")
public class InteractiveConsoleRunner implements CommandLineRunner {

    private static final String DEMO_CUSTOMER = "guest";

    private final CatalogService catalogService;
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final CouponService couponService;

    private final Scanner scanner = new Scanner(System.in);

    public InteractiveConsoleRunner(CatalogService catalogService, CartService cartService,
                                    CheckoutService checkoutService, OrderService orderService,
                                    CouponService couponService) {
        this.catalogService = catalogService;
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.orderService = orderService;
        this.couponService = couponService;
    }

    @Override
    public void run(String... args) {
        System.out.printf("%n=== E-Commerce Demo Console (customer: %s) ===%n", DEMO_CUSTOMER);
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> browse(catalogService.listAll());
                    case "2" -> search();
                    case "3" -> addToCart();
                    case "4" -> viewCart();
                    case "5" -> updateQuantity();
                    case "6" -> removeFromCart();
                    case "7" -> applyCoupon();
                    case "8" -> cartService.removeCoupon(DEMO_CUSTOMER);
                    case "9" -> checkout();
                    case "10" -> orderHistory();
                    case "11" -> cancelOrder();
                    case "12" -> advanceStatus();
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option, please try again.");
                }
            } catch (ECommerceException | IllegalArgumentException | IllegalStateException ex) {
                System.out.println("! " + ex.getMessage());
            }
        }
        System.out.println("Goodbye - thanks for training with us!");
    }

    private void printMenu() {
        System.out.print("""

                1) Browse catalogue        7) Apply coupon
                2) Search products         8) Remove coupon
                3) Add item to cart        9) Checkout
                4) View cart              10) My order history
                5) Update item quantity   11) Cancel an order
                6) Remove item from cart  12) Advance order status
                0) Exit
                Choose: """);
    }

    private void browse(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.printf("%-9s %-28s %-12s %10s %8s%n", "ID", "NAME", "CATEGORY", "PRICE", "STOCK");
        for (Product product : products) {
            System.out.printf("%-9s %-28s %-12s %10s %8d%n",
                    product.getId(), trim(product.getName(), 28),
                    product.getCategory(), money(product.getPrice()),
                    catalogService.getStock(product.getId()));
        }
    }

    private void search() {
        System.out.print("Keyword (name or description): ");
        browse(catalogService.search(scanner.nextLine()));
    }

    private void addToCart() {
        System.out.print("Product id: ");
        String productId = scanner.nextLine().trim();
        int quantity = readInt("Quantity: ");
        cartService.addItem(DEMO_CUSTOMER, productId, quantity);
        System.out.println("Added to cart.");
    }

    private void viewCart() {
        CartResponse cart = cartService.viewCart(DEMO_CUSTOMER);
        if (cart.items().isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.printf("%-9s %-28s %8s %6s %10s%n", "ID", "NAME", "UNIT", "QTY", "LINE");
        cart.items().forEach(line -> System.out.printf("%-9s %-28s %8s %6d %10s%n",
                line.productId(), trim(line.name(), 28), money(line.unitPrice()),
                line.quantity(), money(line.lineTotal())));
        System.out.println("-".repeat(65));
        if (cart.couponCode() != null) {
            System.out.printf("Coupon %s (%s): -%s%n",
                    cart.couponCode(), cart.couponDescription(), money(cart.discount()));
        }
        System.out.printf("Subtotal: %s | Discount: %s | TOTAL: %s%n",
                money(cart.subtotal()), money(cart.discount()), money(cart.total()));
    }

    private void updateQuantity() {
        System.out.print("Product id: ");
        String productId = scanner.nextLine().trim();
        int quantity = readInt("New quantity: ");
        cartService.updateQuantity(DEMO_CUSTOMER, productId, quantity);
        System.out.println("Quantity updated.");
    }

    private void removeFromCart() {
        System.out.print("Product id: ");
        cartService.removeItem(DEMO_CUSTOMER, scanner.nextLine().trim());
        System.out.println("Item removed.");
    }

    private void applyCoupon() {
        List<Coupon> coupons = couponService.list();
        System.out.println("Available coupons:");
        coupons.forEach(coupon -> System.out.println("  " + coupon));
        System.out.print("Code to apply: ");
        Coupon applied = cartService.applyCoupon(DEMO_CUSTOMER, scanner.nextLine());
        System.out.println("Coupon applied: " + applied.describe());
    }

    private void checkout() {
        Order order = checkoutService.checkout(DEMO_CUSTOMER);
        printOrder(order);
    }

    private void orderHistory() {
        List<Order> history = orderService.getOrderHistory(DEMO_CUSTOMER);
        if (history.isEmpty()) {
            System.out.println("You have no orders yet.");
            return;
        }
        history.forEach(this::printOrder);
    }

    private void cancelOrder() {
        System.out.print("Order id to cancel: ");
        Order cancelled = orderService.cancelOrder(scanner.nextLine().trim());
        System.out.println("Cancelled. Stock has been returned to the warehouse.");
        printOrder(cancelled);
    }

    private void advanceStatus() {
        System.out.print("Order id: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderService.getOrder(orderId);
        System.out.printf("Current status: %s%n", order.getStatus());
        System.out.print("New status (SHIPPED / DELIVERED): ");
        OrderStatus target = OrderStatus.valueOf(scanner.nextLine().trim().toUpperCase());
        printOrder(orderService.transitionStatus(orderId, target));
    }

    private void printOrder(Order order) {
        System.out.println("=".repeat(65));
        System.out.printf("Order %s | %s | %s%n", order.getId(), order.getStatus(), order.getCreatedAt());
        order.getItems().forEach(item -> System.out.printf("  %-9s %-28s %6d x %10s = %10s%n",
                item.productId(), trim(item.productName(), 28), item.quantity(),
                money(item.unitPrice()), money(item.lineTotal())));
        System.out.println("-".repeat(65));
        if (order.getCouponCode() != null) {
            System.out.printf("  Coupon %s: -%s%n", order.getCouponCode(), money(order.getDiscount()));
        }
        System.out.printf("  Subtotal: %s | Discount: %s | TOTAL: %s%n",
                money(order.getSubtotal()), money(order.getDiscount()), money(order.getTotal()));
        System.out.println("=".repeat(65));
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private String money(BigDecimal amount) {
        return "\u20B9" + amount.toPlainString();
    }

    private String trim(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "\u2026";
    }
}
