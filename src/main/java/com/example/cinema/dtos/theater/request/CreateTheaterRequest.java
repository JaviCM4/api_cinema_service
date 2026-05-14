package com.example.cinema.dtos.theater.request;

import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.models.theater.TypeTheater;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateTheaterRequest {

    @NotNull(message = "cinemaId is required")
    UUID cinemaId;

    @NotNull(message = "typeTheaterId is required")
    UUID typeTheaterId;

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must not exceed 255 characters")
    String name;

    @NotNull(message = "rows is required")
    @Min(value = 1, message = "rows must be at least 1")
    Integer rows;

    @NotNull(message = "cols is required")
    @Min(value = 1, message = "cols must be at least 1")
    Integer cols;

    public Theater createEntity(Cinema cinema, TypeTheater typeTheater) {
        Theater theater = new Theater();
        theater.setCinema(cinema);
        theater.setTypeTheater(typeTheater);
        theater.setName(this.name);
        theater.setRows(this.rows);
        theater.setCols(this.cols);
        return theater;
    }
}
