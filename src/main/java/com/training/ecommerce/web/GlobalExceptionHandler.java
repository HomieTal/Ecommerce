package com.training.ecommerce.web;

import com.training.ecommerce.domain.exception.CouponNotFoundException;
import com.training.ecommerce.domain.exception.EmptyCartException;
import com.training.ecommerce.domain.exception.ECommerceException;
import com.training.ecommerce.domain.exception.InsufficientStockException;
import com.training.ecommerce.domain.exception.InvalidQuantityException;
import com.training.ecommerce.domain.exception.OrderNotFoundException;
import com.training.ecommerce.domain.exception.ProductNotInCartException;
import com.training.ecommerce.domain.exception.ProductNotFoundException;
import com.training.ecommerce.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * Translates domain exceptions into consistent HTTP responses, so controllers
 * and services stay free of HTTP concerns.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ProductNotFoundException.class, OrderNotFoundException.class,
            ProductNotInCartException.class, CouponNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(ECommerceException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> conflict(ECommerceException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> emptyCart(ECommerceException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({InvalidQuantityException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> stateConflict(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), HttpStatus.CONFLICT.getReasonPhrase());
    }

    /** Bad path/query values, e.g. an unknown ProductCategory in the URL. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        return build(HttpStatus.BAD_REQUEST, message, HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noRoute(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "No such endpoint", HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error",
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex) {
        return build(status, ex.getMessage(), status.getReasonPhrase());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String error) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), error,
                message == null ? "" : message);
        return ResponseEntity.status(status).body(body);
    }
}
