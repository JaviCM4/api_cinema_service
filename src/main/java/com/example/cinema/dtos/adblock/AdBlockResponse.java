package com.example.cinema.dtos.adblock;

import com.example.cinema.models.cinema.AdBlock;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdBlockResponse {
    UUID id;
    UUID cinemaId;
    String cinemaName;
    Integer daysBlocked;
    String startDate;
    String endDate;
    BigDecimal amountPaid;
    LocalDateTime createdAt;

    public static AdBlockResponse fromEntity(AdBlock adBlock) {
        return new AdBlockResponse(
                adBlock.getId(),
                adBlock.getCinema().getId(),
                adBlock.getCinema().getName(),
                adBlock.getDaysBlocked(),
                adBlock.getStartDate().toString(),
                adBlock.getEndDate().toString(),
                adBlock.getAmountPaid(),
                adBlock.getCreatedAt()
        );
    }

}
