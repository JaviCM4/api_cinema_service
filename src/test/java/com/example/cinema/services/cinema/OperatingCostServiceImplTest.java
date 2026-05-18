package com.example.cinema.services.cinema;

import com.example.cinema.dtos.cinema.request.CreateOperatingCostRequest;
import com.example.cinema.dtos.cinema.response.CinemaOperatingCostSummaryResponse;
import com.example.cinema.exceptions.ConflictException;
import com.example.cinema.exceptions.ResourceNotFoundException;
import com.example.cinema.kafka.CinemaEventProducer;
import com.example.cinema.models.cinema.Cinema;
import com.example.cinema.models.cinema.OperatingCost;
import com.example.cinema.repositories.cinema.CinemaRepository;
import com.example.cinema.repositories.cinema.OperatingCostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperatingCostServiceImplTest {

    private static final UUID CINEMA_ID = UUID.randomUUID();

    @Mock private OperatingCostRepository operatingCostRepository;
    @Mock private CinemaRepository  cinemaRepository;
    @Mock private CinemaEventProducer eventProducer;

    @InjectMocks
    private OperatingCostServiceImplementation operatingCostService;

    @Test
    void testCreateOperatingCost() throws Exception {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), effectiveFrom);

        Cinema cinema = buildCinema();
        ArgumentCaptor<OperatingCost> captor = ArgumentCaptor.forClass(OperatingCost.class);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(cinema));
        when(operatingCostRepository.existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom))
                .thenReturn(false);
        when(operatingCostRepository.save(any(OperatingCost.class))).thenReturn(buildOperatingCost());

        // Act
        operatingCostService.createOperatingCost(request);

        // Assert
        assertAll(
                () -> verify(cinemaRepository).findById(CINEMA_ID),
                () -> verify(operatingCostRepository).existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom),
                () -> verify(operatingCostRepository).save(captor.capture()),
                () -> assertEquals(cinema,                  captor.getValue().getCinema()),
                () -> assertEquals(new BigDecimal("150.00"), captor.getValue().getDailyCost()),
                () -> assertEquals(effectiveFrom,            captor.getValue().getEffectiveFrom()),
                () -> verify(eventProducer).publishOperatingCostCreated(any())
        );
    }

    @Test
    void testCreateOperatingCostCinemaNotFound() {
        // Arrange
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), LocalDate.now().plusDays(1));

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.empty());

        // Assert
        assertThrows(ResourceNotFoundException.class,
                () -> operatingCostService.createOperatingCost(request));
        verify(operatingCostRepository, never()).save(any());
    }

    @Test
    void testCreateOperatingCostDateConflict() {
        // Arrange
        LocalDate effectiveFrom = LocalDate.now().plusDays(1);
        CreateOperatingCostRequest request = new CreateOperatingCostRequest(
                CINEMA_ID, new BigDecimal("150.00"), effectiveFrom);

        when(cinemaRepository.findById(CINEMA_ID)).thenReturn(Optional.of(buildCinema()));
        when(operatingCostRepository.existsByCinema_IdAndEffectiveFrom(CINEMA_ID, effectiveFrom))
                .thenReturn(true);

        // Assert
        assertThrows(ConflictException.class,
                () -> operatingCostService.createOperatingCost(request));
        verify(operatingCostRepository, never()).save(any());
    }

    // ─── getAllOperatingCostSummaries ──────────────────────────────────────────

    @Test
    void testGetAllOperatingCostSummaries() {
        // Escenario con fechas relativas al día actual para que sea determinista:
        // cost1 vigente desde hace 7 días, cost2 desde hace 2 días
        // Período 1: 5 días × 500  = 2500
        // Período 2: 2 días × 600  = 1200  → total 3700
        LocalDate today = LocalDate.now();
        LocalDate date1 = today.minusDays(7);
        LocalDate date2 = today.minusDays(2);

        Cinema cinema = buildCinema();
        OperatingCost cost1 = buildOperatingCostWithDate(cinema, new BigDecimal("500.00"), date1);
        OperatingCost cost2 = buildOperatingCostWithDate(cinema, new BigDecimal("600.00"), date2);

        when(cinemaRepository.findAll()).thenReturn(List.of(cinema));
        when(operatingCostRepository.findByCinema_IdOrderByEffectiveFromAsc(CINEMA_ID))
                .thenReturn(List.of(cost1, cost2));

        List<CinemaOperatingCostSummaryResponse> results =
                operatingCostService.getAllOperatingCostSummaries();

        assertEquals(1, results.size());
        CinemaOperatingCostSummaryResponse result = results.get(0);
        assertAll(
                () -> assertEquals(CINEMA_ID,             result.getCinemaId()),
                () -> assertEquals(cinema.getName(),      result.getCinemaName()),
                () -> assertEquals(2,                     result.getRecords().size()),
                () -> assertEquals(new BigDecimal("500.00"),  result.getRecords().get(0).getDailyCost()),
                () -> assertEquals(date1,                 result.getRecords().get(0).getEffectiveFrom()),
                () -> assertEquals(5L,                    result.getRecords().get(0).getActiveDays()),
                () -> assertEquals(new BigDecimal("2500.00"), result.getRecords().get(0).getPeriodCost()),
                () -> assertEquals(new BigDecimal("600.00"),  result.getRecords().get(1).getDailyCost()),
                () -> assertEquals(date2,                 result.getRecords().get(1).getEffectiveFrom()),
                () -> assertEquals(2L,                    result.getRecords().get(1).getActiveDays()),
                () -> assertEquals(new BigDecimal("1200.00"), result.getRecords().get(1).getPeriodCost()),
                () -> assertEquals(new BigDecimal("3700.00"), result.getTotalCost())
        );
    }

    @Test
    void testGetAllOperatingCostSummariesNoCinemas() {
        when(cinemaRepository.findAll()).thenReturn(List.of());

        List<CinemaOperatingCostSummaryResponse> results =
                operatingCostService.getAllOperatingCostSummaries();

        assertTrue(results.isEmpty());
        verify(operatingCostRepository, never()).findByCinema_IdOrderByEffectiveFromAsc(any());
    }

    @Test
    void testGetAllOperatingCostSummariesCinemaWithNoRecords() {
        Cinema cinema = buildCinema();

        when(cinemaRepository.findAll()).thenReturn(List.of(cinema));
        when(operatingCostRepository.findByCinema_IdOrderByEffectiveFromAsc(CINEMA_ID))
                .thenReturn(List.of());

        List<CinemaOperatingCostSummaryResponse> results =
                operatingCostService.getAllOperatingCostSummaries();

        assertEquals(1, results.size());
        CinemaOperatingCostSummaryResponse result = results.get(0);
        assertAll(
                () -> assertEquals(CINEMA_ID,        result.getCinemaId()),
                () -> assertEquals(cinema.getName(), result.getCinemaName()),
                () -> assertTrue(result.getRecords().isEmpty()),
                () -> assertEquals(BigDecimal.ZERO,  result.getTotalCost())
        );
    }

    private Cinema buildCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(CINEMA_ID);
        cinema.setName("Cinepolis Centro");
        cinema.setCreatedAt(LocalDateTime.now());
        cinema.setUpdatedAt(LocalDateTime.now());
        return cinema;
    }

    private OperatingCost buildOperatingCostWithDate(Cinema cinema, BigDecimal dailyCost, LocalDate effectiveFrom) {
        OperatingCost cost = new OperatingCost();
        cost.setId(UUID.randomUUID());
        cost.setCinema(cinema);
        cost.setDailyCost(dailyCost);
        cost.setEffectiveFrom(effectiveFrom);
        cost.setCreatedAt(LocalDateTime.now());
        return cost;
    }

    private OperatingCost buildOperatingCost() {
        OperatingCost cost = new OperatingCost();
        cost.setId(UUID.randomUUID());
        cost.setCinema(buildCinema());
        cost.setDailyCost(new BigDecimal("150.00"));
        cost.setEffectiveFrom(LocalDate.now().plusDays(1));
        cost.setCreatedAt(LocalDateTime.now());
        return cost;
    }
}
