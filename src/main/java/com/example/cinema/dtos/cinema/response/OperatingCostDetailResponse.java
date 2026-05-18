package com.example.cinema.dtos.cinema.response;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class OperatingCostDetailResponse {

    BigDecimal dailyCost;
    LocalDate effectiveFrom;
    long activeDays;
    BigDecimal periodCost;
}
