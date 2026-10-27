package com.ticketflow.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull Long eventId, @Min(1) int quantity) {
}
