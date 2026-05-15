package com.example.cinema.dtos.adblock;

import com.example.cinema.models.cinema.AdBlockPricing;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdBlockPricingResponse {
    UUID id;
    UUID cinemaId;
    String cinemaName;
    String cinemaLocation;
    BigDecimal pricePerDay;
    LocalDateTime updatedAt;

    public static AdBlockPricingResponse fromEntity(AdBlockPricing adBlockPricing) {
        return new AdBlockPricingResponse(
                adBlockPricing.getId(),
                adBlockPricing.getCinema().getId(),
                adBlockPricing.getCinema().getName(),
                adBlockPricing.getCinema().getAddress(),
                adBlockPricing.getPricePerDay(),
                adBlockPricing.getUpdatedAt()
        );
    }

}
