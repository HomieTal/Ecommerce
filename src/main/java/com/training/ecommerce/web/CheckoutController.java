package com.training.ecommerce.web;

import com.training.ecommerce.domain.model.Order;
import com.training.ecommerce.dto.OrderResponse;
import com.training.ecommerce.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Turns a customer's cart into an order. */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<OrderResponse> checkout(@PathVariable String customerId) {
        Order order = checkoutService.checkout(customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderController.toResponse(order));
    }
}
