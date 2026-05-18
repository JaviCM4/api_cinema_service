package com.example.cinema.dtos.adblock;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
public class AdBlockNowResponse {
    boolean isBlocked;
    String message;
    LocalDate blockEndDate;

     public static AdBlockNowResponse blocked(boolean blocked, LocalDate blockEndDate) {
        String message = blocked
                ? "La publicidad del cine está bloqueada hasta el " + blockEndDate
                : "La publicidad del cine no está bloqueada actualmente";
        return new AdBlockNowResponse(blocked, message, blockEndDate);
    }

}
