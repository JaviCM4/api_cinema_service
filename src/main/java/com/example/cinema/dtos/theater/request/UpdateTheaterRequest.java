package com.example.cinema.dtos.theater.request;

import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateTheaterRequest {

    UUID typeTheaterId;

    @Size(max = 255, message = "El nombre no debe superar 255 caracteres")
    String name;

    Boolean isVisible;

    Boolean allowComments;

    Boolean allowRatings;
}
