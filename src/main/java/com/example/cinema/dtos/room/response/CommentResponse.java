package com.example.cinema.dtos.room.response;

import com.example.cinema.models.room.RoomComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CommentResponse {

    UUID id;
    UUID userId;
    String userName;
    String content;
    LocalDateTime createdAt;
    boolean edited;

    public static CommentResponse from(RoomComment comment, String userName) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                userName,
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt() != null
        );
    }
}
