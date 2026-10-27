package com.ticketflow.bookingservice.dto;

import com.ticketflow.bookingservice.model.Booking;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        Long eventId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String status,
        Instant createdAt,
        Instant expiresAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getEventId(), b.getQuantity(), b.getUnitPrice(),
                b.getTotalPrice(), b.getStatus().name(), b.getCreatedAt(), b.getExpiresAt());
    }
}
