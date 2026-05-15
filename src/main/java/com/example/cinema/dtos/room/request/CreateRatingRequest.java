package com.example.cinema.dtos.room.request;

import com.example.cinema.models.room.RoomRating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateRatingRequest {

    @NotNull(message = "El id del usuario es requerido")
    UUID userId;

    @NotNull(message = "La puntuación es requerida")
    @Min(value = 1, message = "La puntuación debe ser mínimo 1")
    @Max(value = 5, message = "La puntuación debe ser máximo 5")
    Short score;

    public RoomRating createEntity() {
        RoomRating rating = new RoomRating();
        rating.setUserId(userId);
        rating.setScore(score);
        return rating;
    }
}
