package com.ticketflow.eventservice.controller;

import com.ticketflow.eventservice.dto.EventRequest;
import com.ticketflow.eventservice.dto.EventResponse;
import com.ticketflow.eventservice.dto.PageResponse;
import com.ticketflow.eventservice.model.Event;
import com.ticketflow.eventservice.model.EventStatus;
import com.ticketflow.eventservice.repository.EventRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventRepository repository;

    public EventController(EventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PageResponse<EventResponse> search(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) String city,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<Event> result = repository.search(blankToNull(q), blankToNull(city), blankToNull(category),
                PageRequest.of(safePage, safeSize, Sort.by("startDate")));
        return new PageResponse<>(
                result.getContent().stream().map(EventResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        Event event = repository.findById(id)
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(EventController::notFound);
        return EventResponse.from(event);
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setCity(request.city());
        event.setCategory(request.category());
        event.setStartDate(request.startDate());
        event.setPrice(request.price());
        event.setCapacity(request.capacity());
        event.setStatus(EventStatus.DRAFT);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(repository.save(event)));
    }

    @PostMapping("/{id}/publish")
    public EventResponse publish(@PathVariable Long id) {
        Event event = repository.findById(id).orElseThrow(EventController::notFound);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un brouillon peut etre publie");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return EventResponse.from(repository.save(event));
    }

    @PostMapping("/{id}/close")
    public EventResponse close(@PathVariable Long id) {
        Event event = repository.findById(id).orElseThrow(EventController::notFound);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un evenement publie peut etre ferme");
        }
        event.setStatus(EventStatus.CLOSED);
        return EventResponse.from(repository.save(event));
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Evenement introuvable");
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
