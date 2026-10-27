package com.ticketflow.bookingservice.service;

import com.ticketflow.bookingservice.client.EventClient;
import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.dto.EventDto;
import com.ticketflow.bookingservice.model.Booking;
import com.ticketflow.bookingservice.model.BookingStatus;
import com.ticketflow.bookingservice.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private final BookingRepository repository;
    private final EventClient eventClient;
    private final long holdMinutes;
    private final int maxQuantityPerOrder;

    public BookingService(BookingRepository repository,
                          EventClient eventClient,
                          @Value("${booking.hold-minutes:15}") long holdMinutes,
                          @Value("${booking.max-quantity-per-order:6}") int maxQuantityPerOrder) {
        this.repository = repository;
        this.eventClient = eventClient;
        this.holdMinutes = holdMinutes;
        this.maxQuantityPerOrder = maxQuantityPerOrder;
    }

    @Transactional
    public Booking create(String customerId, BookingRequest request) {
        if (request.quantity() > maxQuantityPerOrder) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantite maximale par commande : " + maxQuantityPerOrder);
        }
        EventDto event = eventClient.getPublishedEvent(request.eventId());
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evenement introuvable ou non publie");
        }
        Instant now = Instant.now();
        long reserved = repository.reservedQuantity(event.id(), now);
        if (reserved + request.quantity() > event.capacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plus assez de places disponibles");
        }
        Booking booking = new Booking();
        booking.setEventId(event.id());
        booking.setCustomerId(customerId);
        booking.setQuantity(request.quantity());
        booking.setUnitPrice(event.price());
        booking.setTotalPrice(event.price().multiply(BigDecimal.valueOf(request.quantity())));
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreatedAt(now);
        booking.setExpiresAt(now.plus(holdMinutes, ChronoUnit.MINUTES));
        return repository.save(booking);
    }
}
