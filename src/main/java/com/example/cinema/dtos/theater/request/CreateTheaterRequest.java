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

    @NotNull(message = "El id del cine es requerido")
    UUID cinemaId;

    @NotNull(message = "El id del tipo de sala es requerido")
    UUID typeTheaterId;

    @NotBlank(message = "El nombre de la sala es requerido")
    @Size(max = 255, message = "El nombre no debe superar 255 caracteres")
    String name;

    @NotNull(message = "El número de filas es requerido")
    @Min(value = 1, message = "El número de filas debe ser al menos 1")
    Integer rows;

    @NotNull(message = "El número de columnas es requerido")
    @Min(value = 1, message = "El número de columnas debe ser al menos 1")
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
