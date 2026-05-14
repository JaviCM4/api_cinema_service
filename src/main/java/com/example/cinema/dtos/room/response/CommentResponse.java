package com.example.cinema.dtos.room.response;

import com.example.cinema.models.room.RoomComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CommentResponse {

    UUID id;
    UUID userId;
    String content;
    LocalDateTime createdAt;

    public static CommentResponse from(RoomComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
