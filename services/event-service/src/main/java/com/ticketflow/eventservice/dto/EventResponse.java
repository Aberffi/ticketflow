package com.ticketflow.eventservice.dto;

import com.ticketflow.eventservice.model.Event;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String venue,
        String city,
        String category,
        LocalDateTime startDate,
        BigDecimal price,
        int capacity,
        String status) {

    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getVenue(), e.getCity(),
                e.getCategory(), e.getStartDate(), e.getPrice(), e.getCapacity(), e.getStatus().name());
    }
}
