package com.example.cinema.dtos.room.response;

import com.example.cinema.models.room.RoomRating;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class RatingResponse {

    UUID id;
    UUID userId;
    Short score;
    LocalDateTime createdAt;

    public static RatingResponse from(RoomRating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getUserId(),
                rating.getScore(),
                rating.getCreatedAt()
        );
    }
}
