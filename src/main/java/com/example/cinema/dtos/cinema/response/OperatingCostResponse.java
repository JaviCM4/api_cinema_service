package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.OperatingCost;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class OperatingCostResponse {

    UUID id;
    UUID cinemaId;
    BigDecimal dailyCost;
    LocalDate effectiveFrom;
    LocalDateTime createdAt;

    public static OperatingCostResponse from(OperatingCost cost) {
        return new OperatingCostResponse(
                cost.getId(),
                cost.getCinema().getId(),
                cost.getDailyCost(),
                cost.getEffectiveFrom(),
                cost.getCreatedAt()
        );
    }
}
