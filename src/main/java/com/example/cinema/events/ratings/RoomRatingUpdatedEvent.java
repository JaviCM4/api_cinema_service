package com.example.cinema.events.ratings;


import lombok.Value;

import java.util.UUID;

@Value
public class RoomRatingUpdatedEvent {
    UUID ratingId;
    Short score;

    public static RoomRatingUpdatedEvent fromEntity(UUID ratingId, Short score) {
        return new RoomRatingUpdatedEvent(
                ratingId,
                score
        );
    }
}
