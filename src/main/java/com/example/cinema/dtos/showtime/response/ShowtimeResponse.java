package com.example.cinema.dtos.showtime.response;

import com.example.cinema.models.showtime.Showtime;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Value
public class ShowtimeResponse {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    UUID id;
    String theaterName;
    UUID movieId;
    String versionTypeName;
    LocalDate dateShowtime;
    String startShowtime;
    String endShowtime;
    boolean isActive;
    LocalDateTime createdAt;
    String alert;

    public static ShowtimeResponse from(Showtime showtime, String alert) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getTheater().getName(),
                showtime.getMovieId(),
                showtime.getVersionType().getName(),
                showtime.getDateShowtime(),
                showtime.getStartShowtime().format(TIME_FORMAT),
                showtime.getEndShowtime().format(TIME_FORMAT),
                showtime.isActive(),
                showtime.getCreatedAt(),
                alert
        );
    }
}
