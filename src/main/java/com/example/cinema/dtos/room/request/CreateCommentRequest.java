package com.example.cinema.dtos.room.request;

import com.example.cinema.models.room.RoomComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.UUID;

@Value
public class CreateCommentRequest {

    @NotNull(message = "userId is required")
    UUID userId;

    @NotBlank(message = "content is required")
    @Size(min = 1, max = 1000, message = "content must be between 1 and 1000 characters")
    String content;

    public RoomComment createEntity() {
        RoomComment comment = new RoomComment();
        comment.setUserId(userId);
        comment.setContent(content);
        return comment;
    }
}
