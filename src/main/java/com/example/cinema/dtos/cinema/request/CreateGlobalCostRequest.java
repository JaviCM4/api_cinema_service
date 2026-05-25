package com.example.cinema.dtos.cinema.request;

import com.example.cinema.models.cinema.GlobalCost;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class CreateGlobalCostRequest {

    @NotNull(message = "El costo diario es requerido")
    @DecimalMin(value = "0.01", message = "El costo diario debe ser mayor a 0")
    BigDecimal dailyCost;

    @NotNull(message = "La fecha de vigencia es requerida")
    @FutureOrPresent(message = "La fecha de vigencia debe ser hoy o una fecha futura")
    LocalDate effectiveFrom;

    public GlobalCost createEntity() {
        GlobalCost cost = new GlobalCost();
        cost.setDailyCost(dailyCost);
        cost.setEffectiveFrom(effectiveFrom);
        return cost;
    }
}
