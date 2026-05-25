package com.example.cinema.dtos.cinema.response;

import com.example.cinema.models.cinema.Cinema;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CinemaResponse {

    UUID id;
    UUID companyId;
    String companyName;
    UUID adminCinemaId;
    UUID countryId;
    String name;
    String address;
    String phone;
    String email;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getCompany().getId(),
                cinema.getCompany().getName(),
                cinema.getAdminCinemaId(),
                cinema.getCountryId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getPhone(),
                cinema.getEmail(),
                cinema.getCreatedAt(),
                cinema.getUpdatedAt()
        );
    }
}
