package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.GlobalCost;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class GlobalCostResponse {

    BigDecimal dailyCost;
    LocalDate effectiveFrom;

    public static GlobalCostResponse from(GlobalCost cost) {
        return new GlobalCostResponse(
                cost.getDailyCost(),
                cost.getEffectiveFrom()
        );
    }
}
