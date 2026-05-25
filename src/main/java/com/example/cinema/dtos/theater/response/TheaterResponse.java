package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.theater.Theater;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class TheaterResponse {

    UUID id;
    UUID typeTheaterId;
    String typeTheaterName;
    String name;
    Integer rows;
    Integer cols;
    boolean isVisible;
    boolean allowComments;
    boolean allowRatings;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static TheaterResponse from(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getTypeTheater().getId(),
                theater.getTypeTheater().getName(),
                theater.getName(),
                theater.getRows(),
                theater.getCols(),
                theater.isVisible(),
                theater.isAllowComments(),
                theater.isAllowRatings(),
                theater.getCreatedAt(),
                theater.getUpdatedAt()
        );
    }
}
