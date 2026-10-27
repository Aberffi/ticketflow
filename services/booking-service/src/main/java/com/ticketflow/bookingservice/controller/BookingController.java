package com.ticketflow.bookingservice.controller;

import com.ticketflow.bookingservice.dto.BookingRequest;
import com.ticketflow.bookingservice.dto.BookingResponse;
import com.ticketflow.bookingservice.model.Booking;
import com.ticketflow.bookingservice.repository.BookingRepository;
import com.ticketflow.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Temporaire : l'identite du client vient de l'en-tete X-Customer-Id.
 * Elle viendra du jeton JWT quand la securite (Keycloak) sera ajoutee.
 */
@RestController
public class BookingController {

    private final BookingService service;
    private final BookingRepository repository;

    public BookingController(BookingService service, BookingRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping("/api/v1/bookings")
    public ResponseEntity<BookingResponse> create(@RequestHeader("X-Customer-Id") String customerId,
                                                  @Valid @RequestBody BookingRequest request) {
        Booking booking = service.create(customerId, request);
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + booking.getId()))
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/api/v1/bookings/{id}")
    public BookingResponse get(@RequestHeader("X-Customer-Id") String customerId, @PathVariable Long id) {
        return repository.findById(id)
                .filter(b -> b.getCustomerId().equals(customerId))
                .map(BookingResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation introuvable"));
    }

    @GetMapping("/api/v1/me/bookings")
    public List<BookingResponse> mine(@RequestHeader("X-Customer-Id") String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(BookingResponse::from)
                .toList();
    }
}
