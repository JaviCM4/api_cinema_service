package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.CinemaService;
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

@WebMvcTest(CinemaController.class)
@Import(SecurityConfig.class)
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CinemaService cinemaService;

    private static final UUID CINEMA_ID  = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ID   = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID COUNTRY_ID = UUID.fromString("cccccccc-0000-0000-0000-000000000001");

    @Test
    void getAllCinemas_ReturnsList() throws Exception {
        CinemaSummaryResponse summary = new CinemaSummaryResponse(
                CINEMA_ID, "CineMax Premium", "Av. Principal 123", "+1-555-0100", "info@cinemax.com");
        when(cinemaService.findAll()).thenReturn(List.of(summary));

        mockMvc.perform(get("/v1/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CineMax Premium"))
                .andExpect(jsonPath("$[0].email").value("info@cinemax.com"));
    }

    @Test
    void getAllCinemas_EmptyList() throws Exception {
        when(cinemaService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/v1/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCinemaByAdmin_Found() throws Exception {
        CinemaResponse response = new CinemaResponse(
                CINEMA_ID, COUNTRY_ID, "CineMax Premium", "Av. Principal 123",
                "+1-555-0100", "info@cinemax.com", LocalDateTime.now(), LocalDateTime.now());
        when(cinemaService.getByAdminCinemaId(ADMIN_ID)).thenReturn(response);

        mockMvc.perform(get("/v1/cinemas/admin/{adminId}", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CineMax Premium"))
                .andExpect(jsonPath("$.id").value(CINEMA_ID.toString()));
    }

    @Test
    void getCinemaByAdmin_NotFound() throws Exception {
        when(cinemaService.getByAdminCinemaId(ADMIN_ID))
                .thenThrow(new ResourceNotFoundException("Cinema no encontrado"));

        mockMvc.perform(get("/v1/cinemas/admin/{adminId}", ADMIN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCinema_Created() throws Exception {
        String body = """
                {
                    "adminCinemaId": "%s",
                    "countryId": "%s",
                    "name": "CineMax Premium",
                    "email": "info@cinemax.com",
                    "effectiveFrom": "2026-01-01"
                }
                """.formatted(ADMIN_ID, COUNTRY_ID);

        doNothing().when(cinemaService).createCinema(any());

        mockMvc.perform(post("/v1/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void createCinema_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCinema_InvalidEmail_BadRequest() throws Exception {
        String body = """
                {
                    "adminCinemaId": "%s",
                    "countryId": "%s",
                    "name": "CineMax Premium",
                    "email": "no-es-un-email",
                    "effectiveFrom": "2026-01-01"
                }
                """.formatted(ADMIN_ID, COUNTRY_ID);

        mockMvc.perform(post("/v1/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCinema_NoContent() throws Exception {
        String body = """
                {
                    "name": "CineMax Actualizado",
                    "email": "nuevo@cinemax.com"
                }
                """;
        doNothing().when(cinemaService).updateCinema(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCinema_NotFound() throws Exception {
        String body = """
                {
                    "name": "CineMax"
                }
                """;
        doThrow(new ResourceNotFoundException("Cinema no encontrado"))
                .when(cinemaService).updateCinema(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
