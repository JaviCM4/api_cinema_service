package com.example.cinema.dtos.adblock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AdBlockRequest {
    @NotNull(message = "Los días bloqueados no pueden ser nulos")
    @Min(value = 1, message = "Los días bloqueados deben ser al menos 1")
    Integer daysBlocked;

}
