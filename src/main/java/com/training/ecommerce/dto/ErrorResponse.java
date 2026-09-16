package com.training.ecommerce.dto;

import java.time.Instant;

/** Uniform error body returned by the global exception handler. */
public record ErrorResponse(Instant timestamp, int status, String error, String message) {
}
