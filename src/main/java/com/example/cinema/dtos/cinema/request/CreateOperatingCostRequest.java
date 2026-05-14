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

    @NotNull(message = "cinemaId is required")
    UUID cinemaId;

    @NotNull(message = "dailyCost is required")
    @DecimalMin(value = "0.01", message = "dailyCost must be greater than 0")
    BigDecimal dailyCost;

    @NotNull(message = "effectiveFrom is required")
    @FutureOrPresent(message = "effectiveFrom must be today or a future date")
    LocalDate effectiveFrom;

    public OperatingCost createEntity(Cinema cinema) {
        OperatingCost cost = new OperatingCost();
        cost.setCinema(cinema);
        cost.setDailyCost(this.dailyCost);
        cost.setEffectiveFrom(this.effectiveFrom);
        return cost;
    }
}
