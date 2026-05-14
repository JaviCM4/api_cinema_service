package com.example.cinema.dtos.room.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateCommentRequest {

    @NotBlank(message = "content is required")
    @Size(min = 1, max = 1000, message = "content must be between 1 and 1000 characters")
    String content;
}
