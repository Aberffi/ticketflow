package com.ticketflow.bookingservice.client;

import com.ticketflow.bookingservice.dto.EventDto;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Appel synchrone vers event-service (nom logique resolu via Eureka).
 * Au TP4, cette classe sera remplacee par une interface OpenFeign.
 */
@Component
public class EventClient {

    private final RestClient restClient;

    public EventClient(@LoadBalanced RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://event-service").build();
    }

    /** Retourne l'evenement publie, ou null s'il n'existe pas / n'est pas publie. */
    public EventDto getPublishedEvent(Long id) {
        try {
            return restClient.get().uri("/api/v1/events/{id}", id).retrieve().body(EventDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "event-service indisponible");
        }
    }
}
