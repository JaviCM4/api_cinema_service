package com.example.cinema.events.showtimes;

import com.example.cinema.models.showtime.Showtime;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class ShowtimeCreatedEvent {
    UUID      showtimeId;
    UUID      roomId;
    String    roomName;
    UUID      companyId;
    String    companyName;
    UUID movieId;
    LocalDate dateShowtime;
    LocalTime startTime;
    LocalTime endTime;
    LocalDateTime createdAt;

    public static ShowtimeCreatedEvent fromEntity(Showtime showtime) {
        return new ShowtimeCreatedEvent(
                showtime.getId(),
                showtime.getTheater().getId(),
                showtime.getTheater().getName(),
                showtime.getTheater().getCinema().getId(),
                showtime.getTheater().getCinema().getName(),
                showtime.getMovieId(),
                showtime.getDateShowtime(),
                showtime.getStartShowtime(),
                showtime.getEndShowtime(),
                showtime.getCreatedAt()
        );
    }
}
