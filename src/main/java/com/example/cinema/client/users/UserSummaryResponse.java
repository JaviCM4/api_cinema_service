package com.example.cinema.client.users;

import java.util.UUID;

public record UserSummaryResponse(UUID userId, String name) {
}
