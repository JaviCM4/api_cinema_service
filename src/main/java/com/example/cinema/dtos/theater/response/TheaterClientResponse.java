package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.theater.Theater;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class TheaterClientResponse {

    UUID id;
    String typeTheaterName;
    String name;
    Integer rows;
    Integer cols;
    List<ShowtimeInTheaterResponse> showtimes;

    public static TheaterClientResponse from(Theater theater, List<ShowtimeInTheaterResponse> showtimes) {
        return new TheaterClientResponse(
                theater.getId(),
                theater.getTypeTheater().getName(),
                theater.getName(),
                theater.getRows(),
                theater.getCols(),
                showtimes
        );
    }
}
