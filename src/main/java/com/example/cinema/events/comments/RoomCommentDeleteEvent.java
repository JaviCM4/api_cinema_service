package com.example.cinema.events.comments;

import lombok.Value;

import java.util.UUID;

@Value
public class RoomCommentDeleteEvent {
    UUID commentId;

    public static RoomCommentDeleteEvent fromEntity(UUID commentId) {
        return new RoomCommentDeleteEvent(
                commentId
        );
    }
}
