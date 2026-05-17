package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.Cinema;
import lombok.Value;

import java.util.UUID;

@Value
public class CinemaSummaryResponse {

    UUID id;
    String name;
    String address;
    String phone;
    String email;

    public static CinemaSummaryResponse from(Cinema cinema) {
        return new CinemaSummaryResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getPhone(),
                cinema.getEmail()
        );
    }
}
