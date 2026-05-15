package com.example.cinema.dtos.adblock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class AdBlockPricingRequest {
    @NotNull(message="Precio por día es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio por día debe ser mayor que 0")
    BigDecimal pricePerDay;
}
