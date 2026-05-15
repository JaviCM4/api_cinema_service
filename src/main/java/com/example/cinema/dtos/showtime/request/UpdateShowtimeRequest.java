package com.example.cinema.dtos.showtime.request;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
public class UpdateShowtimeRequest {

    UUID movieId;

    UUID versionTypeId;

    LocalDate dateShowtime;

    LocalTime startShowtime;

    LocalTime endShowtime;

    @jakarta.validation.constraints.AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    public boolean isEndAfterStart() {
        if (startShowtime == null || endShowtime == null) return true;
        return endShowtime.isAfter(startShowtime);
    }
}
