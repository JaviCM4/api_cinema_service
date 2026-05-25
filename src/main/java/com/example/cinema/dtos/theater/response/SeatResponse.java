package com.example.cinema.dtos.theater.response;

import com.example.cinema.models.theater.Seat;
import lombok.Value;

import java.util.UUID;

@Value
public class SeatResponse {

    UUID id;
    String rowName;
    Integer colNumber;
    boolean isActive;

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowName(),
                seat.getColNumber(),
                seat.isActive()
        );
    }
}
