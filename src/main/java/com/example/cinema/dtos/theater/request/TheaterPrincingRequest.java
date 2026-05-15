package com.example.cinema.dtos.theater.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class TheaterPrincingRequest {

    @NotNull(message = "El tipo de teatro es requerido")
    UUID typeTheaterId;
    @NotNull(message = "El precio es requerido")
    @Min(value = 0, message = "El precio debe ser un valor positivo")
    BigDecimal price;
    @NotNull(message = "La fecha de vigencia es requerida")
    @FutureOrPresent(message = "La fecha de vigencia debe ser hoy o en el futuro")
    LocalDate effectiveDate;
}
