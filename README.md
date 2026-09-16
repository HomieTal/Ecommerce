# Project 04 — E-Commerce Cart & Order System

Backend business logic of a small e-commerce shop, built as a Spring Boot REST API with an
**in-memory data store**, plus an optional **menu-driven console demo** on top of the same services.

The training goal of this project is to break one large problem into **multiple interacting classes
and services** — and to exercise the core Java curriculum concepts on the way: OOP, interfaces,
collections, streams, enums and exception handling, wrapped in deliberate business rules.

---

## 1. Tech stack

| Layer      | Technology                                        |
|------------|---------------------------------------------------|
| Language   | Java 17 (runs fine on JDK 17/21)                  |
| Framework  | Spring Boot 3.3 (Web)                             |
| Storage    | In-memory (`ConcurrentHashMap`-based repositories)|
| Build      | Maven (`spring-boot-starter-parent`)              |
| Tests      | JUnit 5 + AssertJ (37 tests)                      |

## 2. Quick start

Requirements: JDK 17+ and Maven 3.8+.

```bash
# run all tests
mvn test

# start the REST API (http://localhost:8080)
mvn spring-boot:run

# or run the packaged jar
mvn -DskipTests package
java -jar target/ecommerce-cart-order-system-1.0.0.jar
```

### Interactive console demo

The same services also power a menu-driven console (customer id `guest`):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--app.console.enabled=true --spring.main.web-application-type=none"
```

The console offers: browse, search, add/update/remove cart items, list & apply coupons, checkout,
order history, cancel order, advance order status.

## 3. REST API tour (curl)

Data is seeded on startup: **10 products** (`PRD-1001`…`PRD-1010`) and **4 coupons** —
`SAVE10` (10% off), `FLAT100` (flat 100 off), `BOOK2PLUS1` (buy 2 get 1 free on Clean Code),
`BIGSPEND` (tiered: ≥1000 → 5%, ≥2000 → 10%).

```bash
BASE=http://localhost:8080/api

# ---- catalogue ----
curl -s "$BASE/products"                          # browse (sorted by name)
curl -s "$BASE/products/search?q=java"            # keyword search (name + description)
curl -s "$BASE/products/category/BOOKS"           # filter by enum category
curl -s "$BASE/products/sorted?order=desc"        # sort by price
curl -s "$BASE/products/PRD-1001"                 # one product, with live stock

# ---- cart ----
curl -s -X POST "$BASE/carts/alice/items" \
     -H 'Content-Type: application/json' -d '{"productId":"PRD-1001","quantity":2}'
curl -s -X POST "$BASE/carts/alice/items" \
     -H 'Content-Type: application/json' -d '{"productId":"PRD-1002","quantity":1}'
curl -s -X PATCH "$BASE/carts/alice/items/PRD-1001" \
     -H 'Content-Type: application/json' -d '{"quantity":3}'   # set absolute quantity
curl -s -X POST "$BASE/carts/alice/coupon" \
     -H 'Content-Type: application/json' -d '{"code":"SAVE10"}'
curl -s "$BASE/carts/alice"                       # priced cart: subtotal / discount / total

# ---- checkout & orders ----
curl -s -X POST "$BASE/checkout/alice"            # -> 201, creates the order, clears the cart
curl -s "$BASE/orders/customer/alice"             # order history, newest first
curl -s "$BASE/orders/ORD-1001"
curl -s -X POST "$BASE/orders/ORD-1001/cancel"    # restocks the warehouse
curl -s -X PATCH "$BASE/orders/ORD-1001/status" \
     -H 'Content-Type: application/json' -d '{"status":"SHIPPED"}'

# ---- admin-ish extras ----
curl -s "$BASE/coupons"                           # all campaigns with descriptions
curl -s -X PATCH "$BASE/coupons/SAVE10/active?active=false"
```

### Error handling contract

Business rules surface as meaningful HTTP status codes with a JSON error body
(`timestamp`, `status`, `error`, `message`):

| Situation                                   | Status | Exception                    |
|---------------------------------------------|--------|------------------------------|
| Unknown product / order / coupon            | 404    | `*NotFoundException`         |
| Product not in the cart                     | 404    | `ProductNotInCartException`  |
| Quantity below 1, malformed input           | 400    | `InvalidQuantityException`, `IllegalArgumentException` |
| Empty cart at checkout                      | 409    | `EmptyCartException`         |
| Not enough stock (add / update / checkout)  | 409    | `InsufficientStockException` |
| Illegal status transition, inactive coupon  | 409    | `IllegalStateException`      |

## 4. Architecture

```
com.training.ecommerce
├── domain                  ← pure business model, no Spring imports
│   ├── model               Product, ProductCategory, Cart, CartItem, Coupon,
│   │                       Order, OrderItem, OrderStatus, PriceSummary
│   ├── discount            DiscountStrategy + 4 implementations (Strategy pattern)
│   └── exception           ECommerceException hierarchy (7 concrete types)
├── repository              interfaces + inmemory/ implementations
├── service                 CatalogService, InventoryService, PricingService,
│                           CartService, CouponService, OrderService, CheckoutService
├── web                     REST controllers + GlobalExceptionHandler
├── dto                     request/response records
├── bootstrap               DataSeeder (demo products & coupons)
└── console                 InteractiveConsoleRunner (menu-driven demo)
```

**Layering:** `web → service → repository`, with the domain package at the centre.
Services are the business-logic heart; controllers only translate HTTP ⇄ DTO ⇄ domain.

### Checkout — the most interesting flow

```
CheckoutService.checkout(customerId)
  1. cart must exist and not be empty          → EmptyCartException
  2. re-resolve every line against the live catalogue (price may have changed)
  3. re-check stock for every line             → InsufficientStockException (cart stays intact!)
  4. PricingService prices the cart once       → subtotal, discount (coupon strategy), total
  5. OrderService creates the order            → immutable OrderItem snapshots
  6. InventoryService.reduceStock (all-or-nothing, under a lock)
  7. cart.clear() + save
```

Step 3+6 demonstrate a key business-rule design: **no partial checkouts**. Stock is verified
before pricing, then removed atomically; if anything fails, the cart and warehouse are untouched.

## 5. Key-concept mapping (curriculum → code)

| Concept            | Where to look                                                                                         |
|--------------------|-------------------------------------------------------------------------------------------------------|
| **OOP**            | `Product`, `Cart`, `Order` — state + behaviour + invariants in constructors; `OrderItem` immutable snapshot |
| **Interfaces**     | `ProductRepository`, `CartRepository`, `OrderRepository`, `CouponRepository` vs. `inmemory/*` impls; `DiscountStrategy` |
| **Collections**    | `Cart` (LinkedHashMap of lines), `InventoryService` (ConcurrentHashMap), `requiredQuantities` merging  |
| **Streams**        | `CatalogService.search/listByCategory/sortedByPrice`, `PricingService.summarize` (reduce), `OrderService.getOrderHistory` (sorted) |
| **Enums**          | `ProductCategory`; `OrderStatus` owns its transition rules (`canTransitionTo`, state machine); `DiscountType` + switch expression in `CouponService` |
| **Exception handling** | `domain/exception` hierarchy → `GlobalExceptionHandler` maps each type to an HTTP status; console catches and prints |
| **Business rules** | price > 0; quantity ≥ 1; cart ≤ stock; all-or-nothing checkout; discount capped at subtotal; cancel only while `CONFIRMED`; cancellation restocks |

### The Strategy pattern (discount suite)

Every discount implements `DiscountStrategy#computeDiscount(DiscountContext)`:

| Class                        | Rule                                     | Seeded coupon |
|------------------------------|------------------------------------------|---------------|
| `PercentageDiscountStrategy` | N% off the subtotal                      | `SAVE10`      |
| `FlatAmountDiscountStrategy` | fixed amount off, capped at the subtotal | `FLAT100`     |
| `BuyXGetFreeStrategy`        | buy X get Y free on one product          | `BOOK2PLUS1`  |
| `TieredDiscountStrategy`     | spend-more-save-more brackets            | `BIGSPEND`    |

Adding a campaign = new strategy class + one switch branch in `CouponService`. No existing
code needs to change — that is the open/closed principle in action.

## 6. Test suite (37 tests, JUnit 5 + AssertJ)

Plain unit tests — services are wired by hand over the in-memory repositories (see `TestSupport`),
so the suite runs in milliseconds without a Spring context:

- `CatalogServiceTest` (7) — search, filter, sort, unknown product, invalid price
- `InventoryServiceTest` (6) — negative stock, all-or-nothing reduction, restock
- `CartServiceTest` (9) — merging lines, stock limits, coupon application, priced totals
- `CheckoutServiceTest` (9) — happy path, empty cart, stock dropped mid-flight, all 4 discount types, deactivated coupon, order snapshots
- `OrderServiceTest` (6) — history order, cancel + restock, shipped ≠ cancellable, legal vs illegal transitions

```bash
mvn test
```

## 7. Suggested extensions (extra practice)

1. Add a `CouponValidityChecker` strategy (expiry date, minimum order value, per-customer usage limit).
2. Persist data to CSV/JSON via new repository implementations — the interfaces already exist.
3. Introduce guest vs. registered customers and per-customer order limits.
4. Add request validation annotations (`spring-boot-starter-validation`) and integration tests with `@SpringBootTest` + `MockMvc`.
5. Make `reduceStock` fully concurrent-safe under parallel checkouts (JMeter / ExecutorService experiment).
