package com.example.cinema.dtos.showtime.request;

import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class CreateShowtimeRequest {

    @NotNull(message = "theaterId is required")
    UUID theaterId;

    @NotNull(message = "movieId is required")
    UUID movieId;

    @NotNull(message = "versionTypeId is required")
    UUID versionTypeId;

    @NotNull(message = "dateShowtime is required")
    @FutureOrPresent(message = "dateShowtime must be today or a future date")
    LocalDate dateShowtime;

    @NotNull(message = "startShowtime is required")
    LocalTime startShowtime;

    @NotNull(message = "endShowtime is required")
    LocalTime endShowtime;

    @jakarta.validation.constraints.AssertTrue(message = "endShowtime must be after startShowtime")
    public boolean isEndAfterStart() {
        if (startShowtime == null || endShowtime == null) return true;
        return endShowtime.isAfter(startShowtime);
    }

    public Showtime createEntity(Theater theater, VersionType versionType) {
        Showtime showtime = new Showtime();
        showtime.setTheater(theater);
        showtime.setMovieId(this.movieId);
        showtime.setVersionType(versionType);
        showtime.setDateShowtime(this.dateShowtime);
        showtime.setStartShowtime(this.startShowtime);
        showtime.setEndShowtime(this.endShowtime);
        return showtime;
    }
}
