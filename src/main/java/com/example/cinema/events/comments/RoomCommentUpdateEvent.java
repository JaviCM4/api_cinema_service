package com.example.cinema.events.comments;

import lombok.Value;

import java.util.UUID;

@Value
public class RoomCommentUpdateEvent {
    UUID commentId;
    String content;

    public static RoomCommentUpdateEvent fromEntity(UUID commentId, String content) {
        return new RoomCommentUpdateEvent(
                commentId,
                content
        );
    }
}
