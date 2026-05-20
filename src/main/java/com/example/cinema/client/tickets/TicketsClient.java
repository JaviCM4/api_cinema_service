package com.example.cinema.client.tickets;

import java.util.UUID;

public interface TicketsClient {
    boolean hasTicketsByShowtime(UUID showtimeId);
    boolean hasTicketsByRoomAndUser(UUID roomId, UUID userId);
}
