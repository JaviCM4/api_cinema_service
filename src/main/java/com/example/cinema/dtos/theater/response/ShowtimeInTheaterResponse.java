package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.showtime.Showtime;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Value
public class ShowtimeInTheaterResponse {

    UUID id;
    String versionTypeName;
    LocalDate dateShowtime;
    String startShowtime;
    String endShowtime;
    String alert;

    public static ShowtimeInTheaterResponse from(Showtime showtime, String alert) {
        return new ShowtimeInTheaterResponse(
                showtime.getId(),
                showtime.getVersionType().name(),
                showtime.getDateShowtime(),
                showtime.getStartShowtime().format(DateTimeFormatter.ofPattern("HH:mm")),
                showtime.getEndShowtime().format(DateTimeFormatter.ofPattern("HH:mm")),
                alert
        );
    }
}
