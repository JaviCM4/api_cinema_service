package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.cinema.response.CinemaResponse;
import com.example.cinema.dtos.cinema.response.CinemaSummaryResponse;
import com.example.cinema.dtos.cinema.response.CompanyResponse;
import com.example.cinema.dtos.cinema.request.AssignCinemaAdminRequest;
import com.example.cinema.dtos.cinema.request.CreateCompanyRequest;
import com.example.cinema.exceptions.ConflictException;
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
    private static final UUID COMPANY_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000001");

    @Test
    @WithMockUser
    void getAllCinemas_ReturnsList() throws Exception {
        CinemaSummaryResponse summary = new CinemaSummaryResponse(
                CINEMA_ID, COMPANY_ID, "Cinepolis", ADMIN_ID, "Cinepolis Xela", "Av. Principal 123", "+1-555-0100", "info@cinemax.com");
        when(cinemaService.findAll()).thenReturn(List.of(summary));

        mockMvc.perform(get("/v1/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cinepolis Xela"))
                .andExpect(jsonPath("$[0].companyName").value("Cinepolis"));
    }

    @Test
    @WithMockUser
    void getCompanies_ReturnsList() throws Exception {
        CompanyResponse company = new CompanyResponse(COMPANY_ID, "Cinepolis", LocalDateTime.now(), LocalDateTime.now());
        when(cinemaService.listCompanies()).thenReturn(List.of(company));

        mockMvc.perform(get("/v1/cinemas/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cinepolis"));
    }

    @Test
    void getCinemaByAdmin_Found() throws Exception {
        CinemaResponse response = new CinemaResponse(
                CINEMA_ID, COMPANY_ID, "Cinepolis", ADMIN_ID, COUNTRY_ID, "Cinepolis Xela", "Av. Principal 123",
                "+1-555-0100", "info@cinemax.com", LocalDateTime.now(), LocalDateTime.now());
        when(cinemaService.getByAdminCinemaId(ADMIN_ID)).thenReturn(response);

        mockMvc.perform(get("/v1/cinemas/admin/{adminId}", ADMIN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cinepolis Xela"))
                .andExpect(jsonPath("$.companyName").value("Cinepolis"));
    }

    @Test
    void getCinemaByAdmin_NotFound() throws Exception {
        when(cinemaService.getByAdminCinemaId(ADMIN_ID))
                .thenThrow(new ResourceNotFoundException("Cinema no encontrado"));

        mockMvc.perform(get("/v1/cinemas/admin/{adminId}", ADMIN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createCinema_Created() throws Exception {
        String body = """
                {
                    "companyId": "%s",
                    "adminCinemaId": "%s",
                    "countryId": "%s",
                    "name": "Cinepolis Xela",
                    "email": "info@cinemax.com",
                    "effectiveFrom": "2026-01-01"
                }
                """.formatted(COMPANY_ID, ADMIN_ID, COUNTRY_ID);

        doNothing().when(cinemaService).createCinema(any());

        mockMvc.perform(post("/v1/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createCinema_MissingRequiredFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CINEMA_ADMIN")
    void updateCinema_NoContent() throws Exception {
        String body = """
                {
                    "name": "Cinepolis Xela Centro",
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
    @WithMockUser(roles = "CINEMA_ADMIN")
    void updateCinema_NotFound() throws Exception {
        String body = """
                {
                    "name": "Cinepolis Xela"
                }
                """;
        doThrow(new ResourceNotFoundException("Cinema no encontrado"))
                .when(cinemaService).updateCinema(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ─── POST /v1/cinemas/companies ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createCompany_Created() throws Exception {
        String body = """
                { "name": "Cinepolis" }
                """;
        CompanyResponse resp = new CompanyResponse(COMPANY_ID, "Cinepolis",
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        when(cinemaService.createCompany(any())).thenReturn(resp);

        mockMvc.perform(post("/v1/cinemas/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cinepolis"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createCompany_Conflict() throws Exception {
        String body = """
                { "name": "Cinepolis" }
                """;
        when(cinemaService.createCompany(any()))
                .thenThrow(new ConflictException("La empresa ya existe"));

        mockMvc.perform(post("/v1/cinemas/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createCompany_MissingFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/cinemas/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── PATCH /v1/cinemas/{cinemaId}/admin ───────────────────────────────────

    @Test
    void assignCinemaAdmin_NoContent() throws Exception {
        String body = """
                { "adminCinemaId": "%s" }
                """.formatted(ADMIN_ID);
        doNothing().when(cinemaService).assignCinemaAdmin(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}/admin", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignCinemaAdmin_NotFound() throws Exception {
        String body = """
                { "adminCinemaId": "%s" }
                """.formatted(ADMIN_ID);
        doThrow(new ResourceNotFoundException("Cine no encontrado"))
                .when(cinemaService).assignCinemaAdmin(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}/admin", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignCinemaAdmin_Conflict() throws Exception {
        String body = """
                { "adminCinemaId": "%s" }
                """.formatted(ADMIN_ID);
        doThrow(new ConflictException("El administrador ya esta asignado a otro cine"))
                .when(cinemaService).assignCinemaAdmin(eq(CINEMA_ID), any());

        mockMvc.perform(patch("/v1/cinemas/{id}/admin", CINEMA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
