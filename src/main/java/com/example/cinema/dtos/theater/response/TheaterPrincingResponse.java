package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.theater.TheaterPricing;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
public class TheaterPrincingResponse {
    UUID theaterPricingId;
    UUID theaterId;
    String theaterName;
    UUID typeTheaterId;
    String typeTheaterName;
    BigDecimal price;
    LocalDate effectiveDate;

    public static TheaterPrincingResponse fromEntity(TheaterPricing entity) {
        return new TheaterPrincingResponse(
                entity.getId(),
                entity.getTheater().getId(),
                entity.getTheater().getName(),
                entity.getTypeTheater().getId(),
                entity.getTypeTheater().getName(),
                entity.getPrice(),
                entity.getEffectiveDate()
        );
    }
}
