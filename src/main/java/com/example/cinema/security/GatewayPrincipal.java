package com.example.cinema.security;

import java.util.UUID;

public record GatewayPrincipal(UUID userId, String role) {
}
