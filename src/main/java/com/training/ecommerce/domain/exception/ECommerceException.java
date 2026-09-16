package com.training.ecommerce.domain.exception;

/**
 * Base class of all business exceptions thrown by the e-commerce domain.
 *
 * <p>Extending RuntimeException keeps service signatures clean; the web layer
 * translates each concrete subclass into the right HTTP status code inside the
 * {@code GlobalExceptionHandler}.</p>
 */
public abstract class ECommerceException extends RuntimeException {

    protected ECommerceException(String message) {
        super(message);
    }
}
