package com.example.cinema.client.tickets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class TicketsClientHttp implements TicketsClient {

    private final RestClient restClient;

    @Value("${services.tickets.url}")
    private String ticketsServiceUrl;

    public TicketsClientHttp(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public boolean hasTicketsByShowtime(UUID showtimeId) {
        try {
            Boolean result = restClient.get()
                    .uri(ticketsServiceUrl + "/tickets/internal/has-tickets/showtime/{showtimeId}", showtimeId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException e) {
            return false;
        }
    }

    @Override
    public boolean hasTicketsByRoomAndUser(UUID roomId, UUID userId) {
        try {
            Boolean result = restClient.get()
                    .uri(ticketsServiceUrl + "/tickets/internal/has-tickets/room/{roomId}/user?userId={userId}", roomId, userId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RestClientException e) {
            return false;
        }
    }
}
