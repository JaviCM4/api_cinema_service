package com.example.cinema.events.showtimes;

import com.example.cinema.models.showtime.Showtime;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class ShowtimeUpdatedEvent {
    UUID showtimeId;
    UUID movieId;
    LocalDate dateShowtime;
    LocalTime startTime;
    LocalTime endTime;

    public static ShowtimeUpdatedEvent fromEntity(Showtime showtime) {
        return new ShowtimeUpdatedEvent(
                showtime.getId(),
                showtime.getMovieId(),
                showtime.getDateShowtime(),
                showtime.getStartShowtime(),
                showtime.getEndShowtime()
        );
    }
}
