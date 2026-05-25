package com.example.cinema.dtos.room.response;

import com.example.cinema.models.room.RoomRating;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class UserTheaterRatingResponse {

    UUID id;
    Short score;
    LocalDateTime createdAt;
    UUID theaterId;
    String theaterName;
    UUID cinemaId;
    String cinemaName;
    String cinemaAddress;
    UUID companyId;
    String companyName;

    public static UserTheaterRatingResponse from(RoomRating rating) {
        return new UserTheaterRatingResponse(
                rating.getId(),
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getTheater().getId(),
                rating.getTheater().getName(),
                rating.getTheater().getCinema().getId(),
                rating.getTheater().getCinema().getName(),
                rating.getTheater().getCinema().getAddress(),
                rating.getTheater().getCinema().getCompany().getId(),
                rating.getTheater().getCinema().getCompany().getName()
        );
    }
}
