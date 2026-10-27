package com.ticketflow.eventservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank String title,
        String description,
        @NotBlank String venue,
        @NotBlank String city,
        @NotBlank String category,
        @NotNull @Future LocalDateTime startDate,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(1) int capacity) {
}
