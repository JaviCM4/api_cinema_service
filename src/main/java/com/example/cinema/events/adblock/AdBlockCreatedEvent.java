package com.example.cinema.events.adblock;

import com.example.cinema.models.cinema.AdBlock;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdBlockCreatedEvent {
    UUID adBlockId;
    UUID companyId;
    String companyName;
    Integer daysBlocked;
    BigDecimal amountPaid;
    LocalDate startDate;
    LocalDate endDate;
    LocalDateTime createdAt;

    public static AdBlockCreatedEvent fromEntity(AdBlock adBlock) {
        return new AdBlockCreatedEvent(
                adBlock.getId(),
                adBlock.getCinema().getId(),
                adBlock.getCinema().getName(),
                adBlock.getDaysBlocked(),
                adBlock.getAmountPaid(),
                adBlock.getStartDate(),
                adBlock.getEndDate(),
                adBlock.getCreatedAt()
        );
    }
}
