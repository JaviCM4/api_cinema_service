package com.example.cinema.dtos.cinema.response;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Value
public class CinemaOperatingCostSummaryResponse {

    UUID cinemaId;
    String cinemaName;
    List<OperatingCostDetailResponse> records;
    BigDecimal totalCost;
}
