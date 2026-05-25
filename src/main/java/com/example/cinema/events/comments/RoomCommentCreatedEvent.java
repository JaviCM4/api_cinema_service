package com.example.cinema.events.comments;

import com.example.cinema.models.room.RoomComment;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class RoomCommentCreatedEvent {
    UUID commentId;
    UUID   roomId;
    String roomName;
    UUID   companyId;
    String companyName;
    UUID   userId;
    String content;
    LocalDateTime createdAt;

    public static RoomCommentCreatedEvent fromEntity(RoomComment comment) {
        return new RoomCommentCreatedEvent(
                comment.getId(),
                comment.getTheater().getId(),
                comment.getTheater().getName(),
                comment.getTheater().getCinema().getId(),
                comment.getTheater().getCinema().getName(),
                comment.getUserId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
