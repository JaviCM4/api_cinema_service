package com.example.cinema.dtos.showtime.response;

import com.example.cinema.models.showtime.Showtime;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Value
public class ShowtimeResponse {

    UUID id;
    String theaterName;
    UUID movieId;
    String versionTypeName;
    LocalDate dateShowtime;
    String startShowtime;
    String endShowtime;
    String alert;

    public static ShowtimeResponse from(Showtime showtime, String alert) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getTheater().getName(),
                showtime.getMovieId(),
                showtime.getVersionType().name(),
                showtime.getDateShowtime(),
                showtime.getStartShowtime().format(DateTimeFormatter.ofPattern("HH:mm")),
                showtime.getEndShowtime().format(DateTimeFormatter.ofPattern("HH:mm")),
                alert
        );
    }
}
