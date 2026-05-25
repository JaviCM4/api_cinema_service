package com.example.cinema.controllers;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.dtos.room.response.UserTheaterCommentResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.services.room.inteface.RoomCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class RoomCommentController {

    private final RoomCommentService commentService;

    @Autowired
    public RoomCommentController(RoomCommentService commentService) {
        this.commentService = commentService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/theaters/{theaterId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID theaterId)
            throws ResourceNotFoundException {
        return ResponseEntity.ok(commentService.findCommentsByTheater(theaterId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/v1/theaters/{theaterId}/comments")
    public ResponseEntity<Void> createComment(@PathVariable UUID theaterId, @Valid @RequestBody CreateCommentRequest request)
            throws ResourceNotFoundException, RestrictedException {
        commentService.createComment(theaterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/v1/comments/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable UUID commentId, @Valid @RequestBody UpdateCommentRequest request)
            throws ResourceNotFoundException, ConflictException {
        commentService.updateComment(commentId, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/v1/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId, @RequestParam UUID userId)
            throws ResourceNotFoundException, ConflictException {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/comments/user/{userId}")
    public ResponseEntity<List<UserTheaterCommentResponse>> getCommentsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(commentService.findCommentsByUser(userId));
    }
}
