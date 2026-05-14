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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomCommentServiceImplTest {

    private static final UUID THEATER_ID = UUID.randomUUID();
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock private RoomCommentRepository commentRepository;
    @Mock private TheaterRepository theaterRepository;

    @InjectMocks
    private RoomCommentServiceImplementation commentService;

    @Test
    void testCreateComment() throws Exception {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente sala");
        Theater theater              = buildTheater(true);
        RoomComment saved            = buildComment("Excelente sala");

        ArgumentCaptor<RoomComment> captor = ArgumentCaptor.forClass(RoomComment.class);

        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(theater));
        when(commentRepository.save(any(RoomComment.class))).thenReturn(saved);

        // Act
        commentService.createComment(THEATER_ID, request);

        // Assert
        assertAll(
                () -> verify(theaterRepository).findById(THEATER_ID),
                () -> verify(commentRepository).save(captor.capture()),
                () -> assertEquals(theater,          captor.getValue().getTheater()),
                () -> assertEquals(USER_ID,          captor.getValue().getUserId()),
                () -> assertEquals("Excelente sala", captor.getValue().getContent())
        );
    }

    @Test
    void testCreateCommentTheaterNotFound() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente sala");
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(THEATER_ID, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testCreateCommentWhenCommentsNotAllowed() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(USER_ID, "Excelente sala");
        when(theaterRepository.findById(THEATER_ID)).thenReturn(Optional.of(buildTheater(false)));

        // Assert
        assertThrows(RestrictedException.class,
                () -> commentService.createComment(THEATER_ID, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testUpdateComment() throws Exception {
        // Arrange
        UpdateCommentRequest request    = new UpdateCommentRequest("Contenido actualizado");
        RoomComment existing            = buildComment("Contenido original");
        ArgumentCaptor<RoomComment> captor = ArgumentCaptor.forClass(RoomComment.class);

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any(RoomComment.class))).thenReturn(existing);

        // Act
        commentService.updateComment(COMMENT_ID, request);

        // Assert
        assertAll(
                () -> verify(commentRepository).save(captor.capture()),
                () -> assertEquals("Contenido actualizado", captor.getValue().getContent())
        );
    }

    @Test
    void testUpdateCommentNotFound() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest("Contenido actualizado");
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(COMMENT_ID, request));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testDeleteComment() throws Exception {
        // Arrange
        when(commentRepository.existsById(COMMENT_ID)).thenReturn(true);

        // Act
        commentService.deleteComment(COMMENT_ID);

        // Assert
        verify(commentRepository).deleteById(COMMENT_ID);
    }

    @Test
    void testDeleteCommentNotFound() {
        // Arrange
        when(commentRepository.existsById(COMMENT_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(COMMENT_ID));
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void testFindCommentsByTheater() throws Exception {
        // Arrange
        RoomComment c1 = buildComment("Primer comentario");
        RoomComment c2 = buildComment("Segundo comentario");
        c2.setId(UUID.randomUUID());

        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(commentRepository.findByTheater_IdOrderByCreatedAtDesc(THEATER_ID))
                .thenReturn(List.of(c1, c2));

        // Act
        List<CommentResponse> result = commentService.findCommentsByTheater(THEATER_ID);

        // Assert
        assertAll(
                () -> assertEquals(2,                    result.size()),
                () -> assertEquals("Primer comentario",  result.get(0).getContent()),
                () -> assertEquals("Segundo comentario", result.get(1).getContent())
        );
    }

    @Test
    void testFindCommentsByTheaterEmpty() throws Exception {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(true);
        when(commentRepository.findByTheater_IdOrderByCreatedAtDesc(THEATER_ID))
                .thenReturn(List.of());

        // Act
        List<CommentResponse> result = commentService.findCommentsByTheater(THEATER_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindCommentsByTheaterNotFound() {
        // Arrange
        when(theaterRepository.existsById(THEATER_ID)).thenReturn(false);

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> commentService.findCommentsByTheater(THEATER_ID));
        verify(commentRepository, never()).findByTheater_IdOrderByCreatedAtDesc(any());
    }

    private Theater buildTheater(boolean allowComments) {
        Theater theater = new Theater();
        theater.setId(THEATER_ID);
        theater.setName("Sala 1");
        theater.setAllowComments(allowComments);
        return theater;
    }

    private RoomComment buildComment(String content) {
        RoomComment comment = new RoomComment();
        comment.setId(COMMENT_ID);
        comment.setUserId(USER_ID);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }
}
