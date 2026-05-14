package com.example.cinema.controllers;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.services.room.inteface.RoomCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/theaters/{theaterId}/comments")
public class RoomCommentController {

    private final RoomCommentService commentService;

    @Autowired
    public RoomCommentController(RoomCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID theaterId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(commentService.findCommentsByTheater(theaterId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID theaterId,
            @Valid @RequestBody CreateCommentRequest request)
            throws ResourceNotFoundException, RestrictedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(theaterId, request));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId)
            throws ResourceNotFoundException {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
