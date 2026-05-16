package com.example.cinema.events.ratings;


import com.example.cinema.models.room.RoomRating;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class RoomRatingCreatedEvent {
    UUID ratingId;
    UUID     roomId;
    String   roomName;
    UUID     companyId;
    UUID     userId;
    Short    score;
    LocalDateTime createdAt;

    public static RoomRatingCreatedEvent fromEntity(RoomRating rating) {
        return new RoomRatingCreatedEvent(
                rating.getId(),
                rating.getTheater().getId(),
                rating.getTheater().getName(),
                rating.getTheater().getCinema().getId(),
                rating.getUserId(),
                rating.getScore(),
                rating.getCreatedAt()
        );
    }
}
