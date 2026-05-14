package com.example.cinema.dtos.room.request;

import com.example.cinema.models.room.RoomRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateRatingRequest {

    @NotNull(message = "userId is required")
    UUID userId;

    @NotNull(message = "score is required")
    @Min(value = 1, message = "score must be at least 1")
    @Max(value = 5, message = "score must be at most 5")
    Short score;

    public RoomRating createEntity() {
        RoomRating rating = new RoomRating();
        rating.setUserId(userId);
        rating.setScore(score);
        return rating;
    }
}
