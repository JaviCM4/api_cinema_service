package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.GlobalCost;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class GlobalCostResponse {

    UUID id;
    BigDecimal dailyCost;
    LocalDate effectiveFrom;
    LocalDateTime createdAt;

    public static GlobalCostResponse from(GlobalCost cost) {
        return new GlobalCostResponse(
                cost.getId(),
                cost.getDailyCost(),
                cost.getEffectiveFrom(),
                cost.getCreatedAt()
        );
    }
}
