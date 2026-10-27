package com.ticketflow.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventDto(Long id, String title, BigDecimal price, int capacity, String status) {
}
