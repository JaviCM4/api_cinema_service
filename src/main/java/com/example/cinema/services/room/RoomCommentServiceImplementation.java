package com.example.cinema.services.room;

import com.example.cinema.dtos.room.request.CreateCommentRequest;
import com.example.cinema.dtos.room.request.UpdateCommentRequest;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.events.comments.RoomCommentCreatedEvent;
import com.example.cinema.events.comments.RoomCommentDeleteEvent;
import com.example.cinema.events.comments.RoomCommentUpdateEvent;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.kafka.CinemaEventProducer;
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
    private final CinemaEventProducer eventProducer;

    @Autowired
    public RoomCommentServiceImplementation(RoomCommentRepository commentRepository, TheaterRepository theaterRepository, CinemaEventProducer eventProducer) {
        this.commentRepository = commentRepository;
        this.theaterRepository = theaterRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(UUID theaterId, CreateCommentRequest dto)
            throws ResourceNotFoundException, RestrictedException {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada con id: " + theaterId));

        if (!theater.isAllowComments()) {
            throw new RestrictedException("Los comentarios no están permitidos en esta sala");
        }

        RoomComment comment = dto.createEntity();
        comment.setTheater(theater);

        commentRepository.save(comment);

        // Publicar evento de creacion de comentario
        RoomCommentCreatedEvent event = RoomCommentCreatedEvent.fromEntity(comment);
        eventProducer.publisRoomCommentCreated(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateComment(UUID commentId, UpdateCommentRequest dto)
            throws ResourceNotFoundException, ConflictException {
        RoomComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + commentId));

        if (!comment.getUserId().equals(dto.getUserId())) {
            throw new ConflictException("No tiene permisos para actualizar este comentario porque fue creado por otro usuario");
        }

        comment.setContent(dto.getContent());
        commentRepository.save(comment);

        // Publicar evento de actualizacion de comentario
        RoomCommentUpdateEvent event = RoomCommentUpdateEvent.fromEntity(comment.getId(), comment.getContent());
        eventProducer.publishRoomCommentUpdated(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(UUID commentId, UUID userId) throws ResourceNotFoundException, ConflictException {
        RoomComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new ConflictException("No tiene permisos para eliminar este comentario porque fue creado por otro usuario");
        }

        commentRepository.deleteById(commentId);

        // Publicar evento de eliminacion de comentario
        RoomCommentDeleteEvent event = new RoomCommentDeleteEvent(commentId);
        eventProducer.publishRoomCommentDeleted(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByTheater(UUID theaterId) throws ResourceNotFoundException {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Sala no encontrada con id: " + theaterId);
        }
        return commentRepository.findByTheater_IdOrderByCreatedAtDesc(theaterId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }
}
