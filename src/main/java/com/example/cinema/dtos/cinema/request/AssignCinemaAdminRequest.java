package com.example.cinema.dtos.cinema.request;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class AssignCinemaAdminRequest {

    @NotNull(message = "El id del administrador de cine es requerido")
    UUID adminCinemaId;
}
