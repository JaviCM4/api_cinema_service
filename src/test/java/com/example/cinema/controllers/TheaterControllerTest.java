package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.theater.response.TheaterClientResponse;
import com.example.cinema.dtos.theater.response.TheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.theater.inteface.TheaterService;
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

@WebMvcTest(TheaterController.class)
@Import(SecurityConfig.class)
class TheaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TheaterService theaterService;

    private static final UUID CINEMA_ID       = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001");
    private static final UUID THEATER_ID      = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000011");
    private static final UUID TYPE_THEATER_ID = UUID.randomUUID();
    private static final UUID MOVIE_ID        = UUID.fromString("b0b0b0b0-0000-0000-0000-0000000000A1");

    @Test
    void createTheater_Created() throws Exception {
        String body = """
                {
                    "cinemaId": "%s",
                    "typeTheaterId": "%s",
                    "name": "Sala 2D - A",
                    "rows": 5,
                    "cols": 8
                }
                """.formatted(CINEMA_ID, TYPE_THEATER_ID);

        doNothing().when(theaterService).createTheater(any());

        mockMvc.perform(post("/v1/theaters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void createTheater_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/theaters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTheater_CinemaNotFound() throws Exception {
        String body = """
                {
                    "cinemaId": "%s",
                    "typeTheaterId": "%s",
                    "name": "Sala 2D - A",
                    "rows": 5,
                    "cols": 8
                }
                """.formatted(CINEMA_ID, TYPE_THEATER_ID);

        doThrow(new ResourceNotFoundException("Cinema no encontrado"))
                .when(theaterService).createTheater(any());

        mockMvc.perform(post("/v1/theaters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTheater_NameConflict() throws Exception {
        String body = """
                {
                    "cinemaId": "%s",
                    "typeTheaterId": "%s",
                    "name": "Sala 2D - A",
                    "rows": 5,
                    "cols": 8
                }
                """.formatted(CINEMA_ID, TYPE_THEATER_ID);

        doThrow(new ConflictException("Ya existe una sala con ese nombre"))
                .when(theaterService).createTheater(any());

        mockMvc.perform(post("/v1/theaters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void updateTheater_NoContent() throws Exception {
        String body = """
                {
                    "typeTheaterId": "%s",
                    "name": "Sala 2D - A Actualizada",
                    "isVisible": true,
                    "allowComments": true,
                    "allowRatings": false
                }
                """.formatted(TYPE_THEATER_ID);

        doNothing().when(theaterService).updateTheater(eq(THEATER_ID), any());

        mockMvc.perform(patch("/v1/theaters/{id}", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateTheater_NotFound() throws Exception {
        String body = """
                {
                    "typeTheaterId": "%s",
                    "name": "Sala 2D - A",
                    "isVisible": true,
                    "allowComments": true,
                    "allowRatings": true
                }
                """.formatted(TYPE_THEATER_ID);

        doThrow(new ResourceNotFoundException("Sala no encontrada"))
                .when(theaterService).updateTheater(eq(THEATER_ID), any());

        mockMvc.perform(patch("/v1/theaters/{id}", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTheater_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(patch("/v1/theaters/{id}", THEATER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /v1/theaters?cinemaId=... ─────────────────────────────────────────

    @Test
    void findTheatersByCinema_ReturnsList() throws Exception {
        TheaterResponse theater = new TheaterResponse(
                THEATER_ID, TYPE_THEATER_ID, "2D", "Sala 2D - A",
                5, 8, true, true, true, LocalDateTime.now(), LocalDateTime.now());
        when(theaterService.findTheatersByCinema(CINEMA_ID)).thenReturn(List.of(theater));

        mockMvc.perform(get("/v1/theaters").param("cinemaId", CINEMA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sala 2D - A"))
                .andExpect(jsonPath("$[0].rows").value(5));
    }

    @Test
    void findTheatersByCinema_EmptyList() throws Exception {
        when(theaterService.findTheatersByCinema(CINEMA_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/theaters").param("cinemaId", CINEMA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /v1/theaters/movie?movieId=... ────────────────────────────────────

    @Test
    void findTheatersByMovie_ReturnsList() throws Exception {
        TheaterClientResponse client = new TheaterClientResponse(
                THEATER_ID, "2D", "Sala 2D - A", 5, 8, List.of());
        when(theaterService.findTheatersByMovie(MOVIE_ID)).thenReturn(List.of(client));

        mockMvc.perform(get("/v1/theaters/movie").param("movieId", MOVIE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sala 2D - A"))
                .andExpect(jsonPath("$[0].typeTheaterName").value("2D"));
    }

    @Test
    void findTheatersByMovie_EmptyList() throws Exception {
        when(theaterService.findTheatersByMovie(MOVIE_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/theaters/movie").param("movieId", MOVIE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
