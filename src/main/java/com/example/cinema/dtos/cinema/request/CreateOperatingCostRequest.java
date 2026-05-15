package com.example.cinema.dtos.cinema.request;

import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.OperatingCost;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class CreateOperatingCostRequest {

    @NotNull(message = "El id del cine es requerido")
    UUID cinemaId;

    @NotNull(message = "El costo diario es requerido")
    @DecimalMin(value = "0.01", message = "El costo diario debe ser mayor a 0")
    BigDecimal dailyCost;

    @NotNull(message = "La fecha de vigencia es requerida")
    @FutureOrPresent(message = "La fecha de vigencia debe ser hoy o una fecha futura")
    LocalDate effectiveFrom;

    public OperatingCost createEntity(Cinema cinema) {
        OperatingCost cost = new OperatingCost();
        cost.setCinema(cinema);
        cost.setDailyCost(this.dailyCost);
        cost.setEffectiveFrom(this.effectiveFrom);
        return cost;
    }
}
