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
        return new AdBlockNowResponse(blocked, "La publicidad del cine está bloqueada hasta el " + blockEndDate.toString(), blockEndDate);
    }

}
