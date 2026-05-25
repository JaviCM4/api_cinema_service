package com.example.cinema.dtos.room.response;

import com.example.cinema.models.room.RoomComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class UserTheaterCommentResponse {

    UUID id;
    String content;
    LocalDateTime createdAt;
    boolean edited;
    UUID theaterId;
    String theaterName;
    UUID cinemaId;
    String cinemaName;
    String cinemaAddress;
    UUID companyId;
    String companyName;

    public static UserTheaterCommentResponse from(RoomComment comment) {
        return new UserTheaterCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt() != null,
                comment.getTheater().getId(),
                comment.getTheater().getName(),
                comment.getTheater().getCinema().getId(),
                comment.getTheater().getCinema().getName(),
                comment.getTheater().getCinema().getAddress(),
                comment.getTheater().getCinema().getCompany().getId(),
                comment.getTheater().getCinema().getCompany().getName()
        );
    }
}
