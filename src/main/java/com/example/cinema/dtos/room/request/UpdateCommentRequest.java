package com.example.cinema.dtos.room.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class UpdateCommentRequest {

    @NotNull(message = "El id del usuario es requerido")
    UUID userId;

    @NotBlank(message = "El comentario es requerido")
    @Size(min = 1, max = 1000, message = "El contenido debe tener entre 1 y 1000 caracteres")
    String content;
}
