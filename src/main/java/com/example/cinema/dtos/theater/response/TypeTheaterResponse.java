package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.theater.TypeTheater;
import lombok.Value;

import java.util.UUID;

@Value
public class TypeTheaterResponse {

    UUID id;
    String name;

    public static TypeTheaterResponse from(TypeTheater typeTheater) {
        return new TypeTheaterResponse(typeTheater.getId(), typeTheater.getName());
    }
}
