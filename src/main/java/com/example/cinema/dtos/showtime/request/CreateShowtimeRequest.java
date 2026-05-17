package com.example.cinema.dtos.showtime.request;

import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.VersionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class CreateShowtimeRequest {

    @NotNull(message = "El id de la sala es requerido")
    UUID theaterId;

    @NotNull(message = "El id de la película es requerido")
    UUID movieId;

    @NotNull(message = "El tipo de versión es requerido")
    VersionType versionType;

    @NotNull(message = "La fecha de la función es requerida")
    @FutureOrPresent(message = "La fecha de la función debe ser hoy o una fecha futura")
    LocalDate dateShowtime;

    @NotNull(message = "La hora de inicio es requerida")
    LocalTime startShowtime;

    @NotNull(message = "La hora de fin es requerida")
    LocalTime endShowtime;

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isEndAfterStart() {
        if (startShowtime == null || endShowtime == null) return true;
        return endShowtime.isAfter(startShowtime);
    }

    public Showtime createEntity(Theater theater) {
        Showtime showtime = new Showtime();
        showtime.setTheater(theater);
        showtime.setMovieId(this.movieId);
        showtime.setVersionType(this.versionType);
        showtime.setDateShowtime(this.dateShowtime);
        showtime.setStartShowtime(this.startShowtime);
        showtime.setEndShowtime(this.endShowtime);
        return showtime;
    }
}
