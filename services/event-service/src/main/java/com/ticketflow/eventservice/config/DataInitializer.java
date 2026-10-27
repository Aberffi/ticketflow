package com.ticketflow.eventservice.config;

import com.ticketflow.eventservice.model.Event;
import com.ticketflow.eventservice.model.EventStatus;
import com.ticketflow.eventservice.repository.EventRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedEvents(EventRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(create("Concert Gnawa", "Soiree de musique gnawa", "Theatre Mohammed V", "Rabat",
                    "Concert", LocalDateTime.of(2027, 3, 12, 20, 0), "150.00", 200, EventStatus.PUBLISHED));
            repository.save(create("Conference Spring Cloud", "Microservices avec Spring Cloud", "EMSI Centre",
                    "Casablanca", "Conference", LocalDateTime.of(2027, 4, 5, 9, 30), "0.00", 100,
                    EventStatus.PUBLISHED));
            repository.save(create("Match de gala", "Rencontre caritative", "Stade Ibn Batouta", "Tanger",
                    "Sport", LocalDateTime.of(2027, 5, 20, 18, 0), "80.00", 10, EventStatus.PUBLISHED));
            repository.save(create("Festival brouillon", "Pas encore publie", "Palais des congres", "Marrakech",
                    "Festival", LocalDateTime.of(2027, 6, 1, 19, 0), "120.00", 500, EventStatus.DRAFT));
        };
    }

    private Event create(String title, String description, String venue, String city, String category,
                         LocalDateTime startDate, String price, int capacity, EventStatus status) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setVenue(venue);
        event.setCity(city);
        event.setCategory(category);
        event.setStartDate(startDate);
        event.setPrice(new BigDecimal(price));
        event.setCapacity(capacity);
        event.setStatus(status);
        return event;
    }
}
