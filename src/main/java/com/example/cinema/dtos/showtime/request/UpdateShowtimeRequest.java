package com.example.cinema.dtos.showtime.request;

import com.example.cinema.models.showtime.Showtime;
import com.example.cinema.models.theater.VersionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class UpdateShowtimeRequest {

    @NotNull(message = "El ID de la película es requerido")
    UUID movieId;

    @NotNull(message = "El tipo de versión es requerido")
    VersionType versionType;

    @NotNull(message = "La fecha de la función es requerida")
    @FutureOrPresent(message = "La fecha de la función debe ser hoy o una fecha futura")
    LocalDate dateShowtime;

    @NotNull(message = "La hora de inicio de la función es requerida")
    LocalTime startShowtime;

    @NotNull(message = "La hora de fin de la función es requerida")
    LocalTime endShowtime;

}
