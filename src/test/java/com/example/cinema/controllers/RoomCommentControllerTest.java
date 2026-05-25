package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.room.response.CommentResponse;
import com.example.cinema.dtos.room.response.UserTheaterCommentResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.services.room.inteface.RoomCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomCommentController.class)
@Import(SecurityConfig.class)
class RoomCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomCommentService commentService;

    private static final UUID THEATER_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000011");
    private static final UUID COMMENT_ID = UUID.randomUUID();
    private static final UUID USER_ID    = UUID.fromString("dddddddd-0000-0000-0000-000000000001");

    // ── GET /v1/theaters/{theaterId}/comments ─────────────────────────────────

    @Test
    @WithMockUser
    void getComments_ReturnsList() throws Exception {
        CommentResponse comment = new CommentResponse(
                COMMENT_ID, USER_ID, "Usuario Test", "Excelente sonido e imagen.", LocalDateTime.now(), false);
        when(commentService.findCommentsByTheater(THEATER_ID)).thenReturn(List.of(comment));

        mockMvc.perform(get("/v1/theaters/{theaterId}/comments", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Excelente sonido e imagen."))
                .andExpect(jsonPath("$[0].userId").value(USER_ID.toString()));
    }

    @Test
    @WithMockUser
    void getComments_TheaterNotFound() throws Exception {
        when(commentService.findCommentsByTheater(THEATER_ID))
                .thenThrow(new ResourceNotFoundException("Sala no encontrada"));

        mockMvc.perform(get("/v1/theaters/{theaterId}/comments", THEATER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getComments_EmptyList() throws Exception {
        when(commentService.findCommentsByTheater(THEATER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/theaters/{theaterId}/comments", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /v1/theaters/{theaterId}/comments ────────────────────────────────

    @Test
    @WithMockUser
    void createComment_Created() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Muy buena experiencia en la sala."
                }
                """.formatted(USER_ID);

        doNothing().when(commentService).createComment(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/comments", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void createComment_TheaterNotFound() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Muy buena experiencia."
                }
                """.formatted(USER_ID);

        doThrow(new ResourceNotFoundException("Sala no encontrada"))
                .when(commentService).createComment(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/comments", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createComment_CommentsRestricted_Forbidden() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Muy buena experiencia."
                }
                """.formatted(USER_ID);

        doThrow(new RestrictedException("Los comentarios están desactivados en esta sala"))
                .when(commentService).createComment(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/comments", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createComment_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/theaters/{theaterId}/comments", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /v1/comments/{commentId} ────────────────────────────────────────

    @Test
    @WithMockUser
    void updateComment_NoContent() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Comentario actualizado."
                }
                """.formatted(USER_ID);

        doNothing().when(commentService).updateComment(eq(COMMENT_ID), any());

        mockMvc.perform(patch("/v1/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void updateComment_NotFound() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Comentario actualizado."
                }
                """.formatted(USER_ID);

        doThrow(new ResourceNotFoundException("Comentario no encontrado"))
                .when(commentService).updateComment(eq(COMMENT_ID), any());

        mockMvc.perform(patch("/v1/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateComment_NotOwner_Conflict() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "content": "Comentario actualizado."
                }
                """.formatted(USER_ID);

        doThrow(new ConflictException("No puedes editar el comentario de otro usuario"))
                .when(commentService).updateComment(eq(COMMENT_ID), any());

        mockMvc.perform(patch("/v1/comments/{commentId}", COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ── DELETE /v1/comments/{commentId}?userId=... ────────────────────────────

    @Test
    @WithMockUser
    void deleteComment_NoContent() throws Exception {
        doNothing().when(commentService).deleteComment(eq(COMMENT_ID), eq(USER_ID));

        mockMvc.perform(delete("/v1/comments/{commentId}", COMMENT_ID)
                        .param("userId", USER_ID.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteComment_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Comentario no encontrado"))
                .when(commentService).deleteComment(eq(COMMENT_ID), eq(USER_ID));

        mockMvc.perform(delete("/v1/comments/{commentId}", COMMENT_ID)
                        .param("userId", USER_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteComment_NotOwner_Conflict() throws Exception {
        doThrow(new ConflictException("No puedes eliminar el comentario de otro usuario"))
                .when(commentService).deleteComment(eq(COMMENT_ID), eq(USER_ID));

        mockMvc.perform(delete("/v1/comments/{commentId}", COMMENT_ID)
                        .param("userId", USER_ID.toString()))
                .andExpect(status().isConflict());
    }

    // ── GET /v1/comments/user/{userId} ────────────────────────────────────────

    @Test
    @WithMockUser
    void getCommentsByUser_ReturnsList() throws Exception {
        UserTheaterCommentResponse response = new UserTheaterCommentResponse(
                COMMENT_ID, "Buen sonido", LocalDateTime.now(), false,
                THEATER_ID, "Sala 2D", UUID.randomUUID(), "Cinepolis Centro",
                "Av. Principal 1", UUID.randomUUID(), "Cinepolis");
        when(commentService.findCommentsByUser(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/comments/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Buen sonido"))
                .andExpect(jsonPath("$[0].theaterName").value("Sala 2D"));
    }

    @Test
    @WithMockUser
    void getCommentsByUser_EmptyList() throws Exception {
        when(commentService.findCommentsByUser(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/comments/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
