package com.example.cinema.controllers;

import com.example.cinema.config.SecurityConfig;
import com.example.cinema.dtos.cinema.response.GlobalCostResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.services.cinema.inteface.GlobalCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalCostController.class)
@Import(SecurityConfig.class)
class GlobalCostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GlobalCostService globalCostService;

    // ── POST /v1/global-costs ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createGlobalCost_Created() throws Exception {
        String body = """
                {
                    "dailyCost": 150.00,
                    "effectiveFrom": "2099-12-01"
                }
                """;

        doNothing().when(globalCostService).createGlobalCost(any());

        mockMvc.perform(post("/v1/global-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createGlobalCost_AlreadyExists_Conflict() throws Exception {
        String body = """
                {
                    "dailyCost": 150.00,
                    "effectiveFrom": "2099-12-01"
                }
                """;

        doThrow(new ConflictException("Ya existe un costo global para esa fecha"))
                .when(globalCostService).createGlobalCost(any());

        mockMvc.perform(post("/v1/global-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void createGlobalCost_MissingFields_BadRequest() throws Exception {
        mockMvc.perform(post("/v1/global-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /v1/global-costs ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void getLatestGlobalCost_ReturnsResponse() throws Exception {
        GlobalCostResponse response = new GlobalCostResponse(
                new BigDecimal("150.00"), LocalDate.of(2099, 12, 1));
        when(globalCostService.getLatestGlobalCost()).thenReturn(response);

        mockMvc.perform(get("/v1/global-costs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCost").value(150.00))
                .andExpect(jsonPath("$.effectiveFrom").value("2099-12-01"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void getLatestGlobalCost_NotFound() throws Exception {
        when(globalCostService.getLatestGlobalCost())
                .thenThrow(new ResourceNotFoundException("No existe ningún costo global registrado"));

        mockMvc.perform(get("/v1/global-costs"))
                .andExpect(status().isNotFound());
    }
}
