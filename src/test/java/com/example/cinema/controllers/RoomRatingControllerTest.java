package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.room.response.RatingResponse;
import com.example.cinema.dtos.room.response.RatingSummaryResponse;
import com.example.cinema.dtos.room.response.UserTheaterRatingResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.exceptions.RestrictedException;
import com.example.cinema.services.room.inteface.RoomRatingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomRatingController.class)
@Import(SecurityConfig.class)
class RoomRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRatingService ratingService;

    private static final UUID THEATER_ID = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000011");
    private static final UUID RATING_ID  = UUID.randomUUID();
    private static final UUID USER_ID    = UUID.fromString("dddddddd-0000-0000-0000-000000000001");

    @Test
    @WithMockUser
    void getRatings_ReturnsSummary() throws Exception {
        RatingResponse ratingResponse = new RatingResponse(RATING_ID, USER_ID, (short) 5, LocalDateTime.now());
        RatingSummaryResponse summary = new RatingSummaryResponse(List.of(ratingResponse), 5.0);
        when(ratingService.findRatingsByTheater(THEATER_ID)).thenReturn(summary);

        mockMvc.perform(get("/v1/theaters/{theaterId}/ratings", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(5.0))
                .andExpect(jsonPath("$.ratings[0].score").value(5));
    }

    @Test
    @WithMockUser
    void getRatings_TheaterNotFound() throws Exception {
        when(ratingService.findRatingsByTheater(THEATER_ID))
                .thenThrow(new ResourceNotFoundException("Sala no encontrada"));

        mockMvc.perform(get("/v1/theaters/{theaterId}/ratings", THEATER_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getRatings_EmptySummary() throws Exception {
        RatingSummaryResponse summary = new RatingSummaryResponse(List.of(), null);
        when(ratingService.findRatingsByTheater(THEATER_ID)).thenReturn(summary);

        mockMvc.perform(get("/v1/theaters/{theaterId}/ratings", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratings").isEmpty());
    }

    @Test
    @WithMockUser
    void createRating_Created() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 4
                }
                """.formatted(USER_ID);

        doNothing().when(ratingService).createRating(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void createRating_TheaterNotFound() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 4
                }
                """.formatted(USER_ID);

        doThrow(new ResourceNotFoundException("Sala no encontrada"))
                .when(ratingService).createRating(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createRating_RatingsRestricted_Forbidden() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 3
                }
                """.formatted(USER_ID);

        doThrow(new RestrictedException("Las calificaciones están desactivadas en esta sala"))
                .when(ratingService).createRating(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void createRating_AlreadyRated_Conflict() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 3
                }
                """.formatted(USER_ID);

        doThrow(new ConflictException("El usuario ya calificó esta sala"))
                .when(ratingService).createRating(eq(THEATER_ID), any());

        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void createRating_ScoreOutOfRange_BadRequest() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 10
                }
                """.formatted(USER_ID);

        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createRating_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/theaters/{theaterId}/ratings", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateRating_NoContent() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 5
                }
                """.formatted(USER_ID);

        doNothing().when(ratingService).updateRating(eq(RATING_ID), any());

        mockMvc.perform(patch("/v1/ratings/{ratingId}", RATING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void updateRating_NotFound() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 5
                }
                """.formatted(USER_ID);

        doThrow(new ResourceNotFoundException("Calificación no encontrada"))
                .when(ratingService).updateRating(eq(RATING_ID), any());

        mockMvc.perform(patch("/v1/ratings/{ratingId}", RATING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateRating_NotOwner_Conflict() throws Exception {
        String body = """
                {
                    "userId": "%s",
                    "score": 5
                }
                """.formatted(USER_ID);

        doThrow(new ConflictException("No puedes modificar la calificación de otro usuario"))
                .when(ratingService).updateRating(eq(RATING_ID), any());

        mockMvc.perform(patch("/v1/ratings/{ratingId}", RATING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ── GET /v1/ratings/user/{userId} ────────────────────────────────────────

    @Test
    @WithMockUser
    void getRatingsByUser_ReturnsList() throws Exception {
        UserTheaterRatingResponse response = new UserTheaterRatingResponse(
                RATING_ID, (short) 5, LocalDateTime.now(),
                THEATER_ID, "Sala IMAX", UUID.randomUUID(), "Cinepolis Sur",
                "Av. Sur 10", UUID.randomUUID(), "Cinepolis");
        when(ratingService.findRatingsByUser(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/ratings/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[0].theaterName").value("Sala IMAX"));
    }

    @Test
    @WithMockUser
    void getRatingsByUser_EmptyList() throws Exception {
        when(ratingService.findRatingsByUser(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/ratings/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
