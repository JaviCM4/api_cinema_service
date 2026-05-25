package com.example.cinema.services.room.inteface;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.dtos.room.response.UserTheaterCommentResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;

import java.util.List;
import java.util.UUID;

public interface RoomCommentService {

    void createComment(UUID theaterId, CreateCommentRequest dto)
            throws ResourceNotFoundException, RestrictedException;

    void updateComment(UUID commentId, UpdateCommentRequest dto)
            throws ResourceNotFoundException, ConflictException;

    void deleteComment(UUID commentId, UUID userId) throws ResourceNotFoundException, ConflictException;

    List<CommentResponse> findCommentsByTheater(UUID theaterId) throws ResourceNotFoundException;

    List<UserTheaterCommentResponse> findCommentsByUser(UUID userId);
}
