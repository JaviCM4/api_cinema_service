package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.showtime.response.ShowtimeByTheaterResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.showtime.inteface.ShowtimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(ShowtimeController.class)
@Import(SecurityConfig.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeService showtimeService;

    private static final UUID SHOWTIME_ID = UUID.fromString("b1b2b3b4-0000-0000-0000-000000000001");
    private static final UUID THEATER_ID  = UUID.fromString("c1c2c3c4-0000-0000-0000-000000000001");
    private static final UUID MOVIE_ID    = UUID.fromString("d1d2d3d4-0000-0000-0000-000000000001");

    // ─── GET /v1/showtimes/theater/{theaterId} ────────────────────────────────

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void getShowtimesByTheater_ReturnsList() throws Exception {
        ShowtimeByTheaterResponse resp = new ShowtimeByTheaterResponse(
                SHOWTIME_ID, MOVIE_ID, "ORIGINAL", LocalDate.now().plusDays(1),
                "14:00", "16:00", true, null);
        when(showtimeService.findAllShowtimesByTheaterForAdmin(THEATER_ID)).thenReturn(List.of(resp));

        mockMvc.perform(get("/v1/showtimes/theater/{theaterId}", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieId").value(MOVIE_ID.toString()))
                .andExpect(jsonPath("$[0].versionTypeName").value("ORIGINAL"));
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void getShowtimesByTheater_EmptyList() throws Exception {
        when(showtimeService.findAllShowtimesByTheaterForAdmin(THEATER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/v1/showtimes/theater/{theaterId}", THEATER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── POST /v1/showtimes ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void createShowtime_Created() throws Exception {
        String body = """
                {
                    "theaterId": "%s",
                    "movieId": "%s",
                    "versionType": "ORIGINAL",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "14:00:00",
                    "endShowtime": "16:00:00"
                }
                """.formatted(THEATER_ID, MOVIE_ID);

        doNothing().when(showtimeService).createShowtime(any());

        mockMvc.perform(post("/v1/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void createShowtime_TheaterNotFound() throws Exception {
        String body = """
                {
                    "theaterId": "%s",
                    "movieId": "%s",
                    "versionType": "ORIGINAL",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "14:00:00",
                    "endShowtime": "16:00:00"
                }
                """.formatted(THEATER_ID, MOVIE_ID);

        doThrow(new ResourceNotFoundException("Sala no encontrada"))
                .when(showtimeService).createShowtime(any());

        mockMvc.perform(post("/v1/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void createShowtime_Overlap_Conflict() throws Exception {
        String body = """
                {
                    "theaterId": "%s",
                    "movieId": "%s",
                    "versionType": "ORIGINAL",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "14:00:00",
                    "endShowtime": "16:00:00"
                }
                """.formatted(THEATER_ID, MOVIE_ID);

        doThrow(new ConflictException("El horario se traslapa"))
                .when(showtimeService).createShowtime(any());

        mockMvc.perform(post("/v1/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void createShowtime_MissingFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── PATCH /v1/showtimes/{showtimeId} ────────────────────────────────────

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void updateShowtime_NoContent() throws Exception {
        String body = """
                {
                    "movieId": "%s",
                    "versionType": "DUBBED",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "18:00:00",
                    "endShowtime": "20:00:00"
                }
                """.formatted(MOVIE_ID);

        doNothing().when(showtimeService).updateShowtime(eq(SHOWTIME_ID), any());

        mockMvc.perform(patch("/v1/showtimes/{id}", SHOWTIME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void updateShowtime_NotFound() throws Exception {
        String body = """
                {
                    "movieId": "%s",
                    "versionType": "ORIGINAL",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "14:00:00",
                    "endShowtime": "16:00:00"
                }
                """.formatted(MOVIE_ID);

        doThrow(new ResourceNotFoundException("Función no encontrada"))
                .when(showtimeService).updateShowtime(eq(SHOWTIME_ID), any());

        mockMvc.perform(patch("/v1/showtimes/{id}", SHOWTIME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void updateShowtime_HasTickets_Conflict() throws Exception {
        String body = """
                {
                    "movieId": "%s",
                    "versionType": "ORIGINAL",
                    "dateShowtime": "2099-12-31",
                    "startShowtime": "14:00:00",
                    "endShowtime": "16:00:00"
                }
                """.formatted(MOVIE_ID);

        doThrow(new ConflictException("No se puede modificar la función porque ya tiene tickets vendidos"))
                .when(showtimeService).updateShowtime(eq(SHOWTIME_ID), any());

        mockMvc.perform(patch("/v1/showtimes/{id}", SHOWTIME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
