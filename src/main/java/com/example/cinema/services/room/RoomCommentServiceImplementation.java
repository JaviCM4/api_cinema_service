package com.example.cinema.services.room;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.models.room.RoomComment;
import com.example.cinema.models.theater.Theater;
import com.example.cinema.repositories.room.RoomCommentRepository;
import com.example.cinema.repositories.theater.TheaterRepository;
import com.example.cinema.services.room.inteface.RoomCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomCommentServiceImplementation implements RoomCommentService {

    private final RoomCommentRepository commentRepository;
    private final TheaterRepository theaterRepository;

    @Autowired
    public RoomCommentServiceImplementation(RoomCommentRepository commentRepository,
                                            TheaterRepository theaterRepository) {
        this.commentRepository = commentRepository;
        this.theaterRepository = theaterRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse createComment(UUID theaterId, CreateCommentRequest dto)
            throws ResourceNotFoundException, RestrictedException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + theaterId));

        if (!theater.isAllowComments()) {
            throw new RestrictedException("Comments are not allowed for this theater");
        }

        RoomComment comment = dto.createEntity();
        comment.setTheater(theater);
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest dto)
            throws ResourceNotFoundException {
        RoomComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comment.setContent(dto.getContent());
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId) throws ResourceNotFoundException {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByTheater(UUID theaterId) throws ResourceNotFoundException {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater not found with id: " + theaterId);
        }
        return commentRepository.findByTheater_IdOrderByCreatedAtDesc(theaterId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }
}
