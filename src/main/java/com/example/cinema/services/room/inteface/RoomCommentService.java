package com.example.cinema.services.room.inteface;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;

import java.util.List;
import java.util.UUID;

public interface RoomCommentService {

    CommentResponse createComment(UUID theaterId, CreateCommentRequest dto)
            throws ResourceNotFoundException, RestrictedException;

    CommentResponse updateComment(UUID commentId, UpdateCommentRequest dto)
            throws ResourceNotFoundException;

    void deleteComment(UUID commentId) throws ResourceNotFoundException;

    List<CommentResponse> findCommentsByTheater(UUID theaterId) throws ResourceNotFoundException;
}
