package com.training.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Project 04 - E-Commerce Cart &amp; Order System.
 *
 * <p>Entry point of the Spring Boot application. The application implements the
 * backend business logic of a small e-commerce shop:</p>
 * <ul>
 *   <li>Product catalogue + product search</li>
 *   <li>Customer carts with quantity management</li>
 *   <li>Discounts via pluggable strategies (percentage, flat amount, buy-X-get-Y-free, tiered)</li>
 *   <li>Checkout with all-or-nothing inventory reservation</li>
 *   <li>Order creation, order history and order lifecycle management</li>
 * </ul>
 *
 * <p>Run modes:</p>
 * <ol>
 *   <li><b>REST API</b> (default): {@code mvn spring-boot:run}</li>
 *   <li><b>Interactive console</b>:
 *       {@code mvn spring-boot:run -Dspring-boot.run.arguments="--app.console.enabled=true --spring.main.web-application-type=none"}</li>
 * </ol>
 */
@SpringBootApplication
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}
