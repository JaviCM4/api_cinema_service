package com.example.cinema.dtos.theater.request;

import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateTheaterRequest {

    UUID typeTheaterId;

    @Size(max = 255, message = "name must not exceed 255 characters")
    String name;

    Boolean isVisible;

    Boolean allowComments;

    Boolean allowRatings;
}
