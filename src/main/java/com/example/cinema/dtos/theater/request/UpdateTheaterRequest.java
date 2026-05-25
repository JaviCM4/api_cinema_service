package com.example.cinema.dtos.theater.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateTheaterRequest {

    @NotNull(message = "El ID del tipo de sala es requerido")
    UUID typeTheaterId;

    @NotBlank(message = "El nombre de la sala es requerido")
    @Size(max = 255, message = "El nombre no debe superar 255 caracteres")
    String name;

    @NotNull(message = "La visibilidad de la sala debe especificarse")
    Boolean isVisible;

    @NotNull(message = "Debe especificarse si se permiten comentarios")
    Boolean allowComments;

    @NotNull(message = "Debe especificarse si se permiten calificaciones")
    Boolean allowRatings;
}
