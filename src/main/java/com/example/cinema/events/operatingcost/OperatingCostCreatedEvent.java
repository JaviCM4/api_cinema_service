package com.example.cinema.events.operatingcost;

import com.example.cinema.models.cinema.OperatingCost;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class OperatingCostCreatedEvent {
    UUID operatingCostId;
    UUID companyId;
    String companyName;
    BigDecimal dailyCost;
    LocalDate effectiveFrom;
    LocalDateTime createdAt;

    public static OperatingCostCreatedEvent fromEntity(OperatingCost operatingCost) {
        return new OperatingCostCreatedEvent(
                operatingCost.getId(),
                operatingCost.getCinema().getId(),
                operatingCost.getCinema().getName(),
                operatingCost.getDailyCost(),
                operatingCost.getEffectiveFrom(),
                operatingCost.getCreatedAt()
        );
    }
}
